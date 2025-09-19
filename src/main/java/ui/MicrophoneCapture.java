package ui;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import core.Main;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import javax.sound.sampled.*;
import java.util.Arrays;

// Bless ChatGPT, DeepSeek and Gemini XD

public class MicrophoneCapture {

    private AudioDispatcher dispatcher;
    private Canvas canvas;
    private boolean running = false;
    private final int bands = 128;
    private GraphicsContext gc;

    // Para el suavizado de la animación
    private final double[] currentHeights;
    private final double[] targetHeights;
    private final double SMOOTHING_FACTOR = 0.75;

    private boolean isResetting = false;
    private int resetFrames = 0;
    private final int MAX_RESET_FRAMES = 2;

    public MicrophoneCapture(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.currentHeights = new double[bands];
        this.targetHeights = new double[bands];

        gc.setFill((Color.color(39/255.0, 39/255.0, 39/255.0)));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void setCanvas(Canvas newCanvas) {
        this.canvas = newCanvas;
        this.gc = newCanvas.getGraphicsContext2D();

        gc.setFill(Color.color(39/255.0, 39/255.0, 39/255.0));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void resetBars() {
        Platform.runLater(() -> {
            double initialHeight = canvas.getHeight() * 0.9;
            Arrays.fill(currentHeights, initialHeight);
            Arrays.fill(targetHeights, initialHeight);

            isResetting = true;
            resetFrames = 0;
        });
    }

    public void start() {
        if (running) return;

        try {
            // Configuración de TarsosDSP - mucho más eficiente

            dispatcher = createDispatcherFromMicName(Main.micDevice, 4096, 0);

            // Procesador para el análisis espectral
            dispatcher.addAudioProcessor(new AudioProcessor() {
                @Override
                public boolean process(AudioEvent audioEvent) {
                    float[] audioBuffer = audioEvent.getFloatBuffer();
                    double[] spectrum = calculateSpectrum(audioBuffer);

                    // Actualizar alturas
                    for (int i = 0; i < bands; i++) {
                        double magnitude = spectrum[i] * canvas.getHeight() * 3.0;

                        if (isResetting) {
                            // Durante el reset, hacemos decaer currentHeights lentamente
                            currentHeights[i] *= 0.95;
                            currentHeights[i] = Math.max(currentHeights[i], magnitude);
                            targetHeights[i] = currentHeights[i]; // sincronizar target con current
                        } else {
                            // Interpolación normal hacia el espectro real
                            targetHeights[i] = Math.min(magnitude, canvas.getHeight() * 0.9);
                            currentHeights[i] += (targetHeights[i] - currentHeights[i]) * SMOOTHING_FACTOR;
                        }
                    }

                    // Contador de frames de reset
                    if (isResetting) {
                        resetFrames++;
                        if (resetFrames >= MAX_RESET_FRAMES) {
                            isResetting = false; // desactiva el modo reset después de unos frames
                        }
                    }

                    // Dibujar el espectro en el hilo de JavaFX
                    drawSpectrum(currentHeights);

                    return true;
                }

                @Override
                public void processingFinished() {
                    // limpieza al terminar si es necesario
                }
            });


            running = true;

            // Ejecutar en un hilo separado - ¡NO BLOQUEA JavaFX!
            Thread audioThread = new Thread(dispatcher, "TarsosDSP Audio Thread");
            audioThread.setDaemon(true);
            audioThread.start();

        } catch (Exception ex) {
            System.err.println("Error initializing TarsosDSP: " + ex.getMessage());
        }
    }

    public void stop() {
        if (!running) return;
        running = false;

        if (dispatcher != null) {
            dispatcher.stop();
        }
    }

    /**
     * Dibuja el espectro en el canvas (debe ejecutarse en el hilo de JavaFX)
     */
    private void drawSpectrum(double[] heights) {
        // Aseguramos que se ejecute en el hilo de JavaFX
        javafx.application.Platform.runLater(() -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();
            double bandWidth = width / bands;

            double padding = 20;
            double arc = 20;

            gc.setFill(Color.rgb(30, 30, 30));
            gc.fillRoundRect(
                    padding,
                    padding,
                    canvas.getWidth()  - padding * 2,
                    canvas.getHeight() - padding * 2,
                    arc,
                    arc
            );

            gc.setStroke(Color.rgb(80, 80, 80));
            gc.setLineWidth(1);
            gc.strokeRoundRect(
                    padding,
                    padding,
                    canvas.getWidth()  - padding * 2,
                    canvas.getHeight() - padding * 2,
                    arc,
                    arc
            );

            // El centro vertical del canvas
            double centerY = height / 2;

            /*
            for (int i = 0; i < bands; i++) {
                double barHeight = heights[i];
                double x = i * bandWidth;

                // El color cambia de azul a rojo a lo largo del espectro
                double hue = 240 - (i * 240.0 / bands);
                Color barColor = Color.hsb(hue, 1.0, 1.0);

                gc.setStroke(barColor);
                gc.setLineWidth(2);

                // Dibuja una única línea vertical centrada
                gc.strokeLine(x, centerY - barHeight / 2, x, centerY + barHeight / 2);
            }
            */
            Color startColor = Color.web("#00FF99");
            Color endColor = Color.web("#0071FF");

            for (int i = 0; i < bands; i++) {
                double barHeight = heights[i];
                double x = i * bandWidth;

                // Interpolación lineal del color
                double t = (double) i / (bands - 1); // 0.0 a 1.0
                double r = startColor.getRed() + t * (endColor.getRed() - startColor.getRed());
                double g = startColor.getGreen() + t * (endColor.getGreen() - startColor.getGreen());
                double b = startColor.getBlue() + t * (endColor.getBlue() - startColor.getBlue());
                Color barColor = new Color(r, g, b, 1.0);

                gc.setStroke(barColor);
                gc.setLineWidth(2);

                // Dibuja una única línea vertical centrada
                gc.strokeLine(x, centerY - barHeight / 2, x, centerY + barHeight / 2);
            }
        });
    }

    /**
     * Calcula el espectro a partir de los samples de audio (ya procesados por TarsosDSP)
     */
    private double[] calculateSpectrum(float[] audioSamples) {
        double[] spectrum = new double[bands];
        int samplesPerBand = audioSamples.length / bands;

        for (int i = 0; i < bands; i++) {
            double sum = 0;
            int start = i * samplesPerBand;
            int end = Math.min(start + samplesPerBand, audioSamples.length);

            for (int j = start; j < end; j++) {
                sum += Math.abs(audioSamples[j]);
            }
            spectrum[i] = sum / samplesPerBand;
        }

        return spectrum;
    }

    public AudioDispatcher createDispatcherFromMicName(String micName, int bufferSize, int overlap) throws LineUnavailableException {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();

        for (Mixer.Info mixerInfo : mixers) {
            if (mixerInfo.getName().startsWith("Port ")) continue;

            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info[] targetLines = mixer.getTargetLineInfo();
            if (targetLines.length > 0 && mixerInfo.getName().toLowerCase().contains(micName.toLowerCase())) {
                TargetDataLine line = (TargetDataLine) mixer.getLine(targetLines[0]);
                AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
                line.open(format, bufferSize);
                line.start();

                AudioInputStream audioStream = new AudioInputStream(line);
                JVMAudioInputStream tarsosStream = new JVMAudioInputStream(audioStream);

                return new AudioDispatcher(tarsosStream, bufferSize, overlap);
            }
        }

        throw new LineUnavailableException("Microphone not found: " + micName);
    }
}
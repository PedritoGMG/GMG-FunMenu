package core;

public class JustSYSOListener implements LineListener{

	@Override
	public void onNewLine(String line) {
		System.out.println(line);
	}

}

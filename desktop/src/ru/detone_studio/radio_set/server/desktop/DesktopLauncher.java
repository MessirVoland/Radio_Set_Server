package ru.detone_studio.radio_set.server.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ru.detone_studio.radio_set.server.Main_Class;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width=480;
		config.height=800;
		new LwjglApplication(new Main_Class(), config);
	}
}

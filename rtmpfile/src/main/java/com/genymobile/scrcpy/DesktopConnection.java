package com.genymobile.scrcpy;

import android.util.Log;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketImpl;
import java.nio.charset.StandardCharsets;

public final class DesktopConnection implements Closeable {

	private static final int DEVICE_NAME_FIELD_LENGTH = 64;

	public final Socket socket;

	private final InputStream inputStream;

	private final ControlEventReader reader = new ControlEventReader();

	private final FileDescriptor fd;

	private DesktopConnection(Socket socket) throws IOException {
		this.socket = socket;
		inputStream = socket.getInputStream();
		fd = getFileDescriptor(socket);
	}
	public FileDescriptor getFileDescriptor(Socket socket) {
		Class<? extends Socket> claz = socket.getClass();
		try {
			Field impl_field = claz.getDeclaredField("impl");
			impl_field.setAccessible(true);
			SocketImpl object = (SocketImpl) impl_field.get(socket);
			Class<? extends SocketImpl> class1 = object.getClass();
			Class<?> superclass = class1.getSuperclass();
			Class<?> superclass2 = superclass.getSuperclass();
			Class<?> superclass3 = superclass2.getSuperclass();
			Method declaredMethod = superclass3.getDeclaredMethod("getFileDescriptor");
			declaredMethod.setAccessible(true);
			FileDescriptor fd = (FileDescriptor) declaredMethod.invoke(object);
			return fd;

		} catch (NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static Socket connect(String hostname, int port) throws IOException {
		Socket socket = new Socket(hostname, port);
//		new SocketAddress(abstractName)
		boolean connected = socket.isConnected();
		Log.i("StartActivity","socketB  status :" + connected);

		return socket;
	}

	public static DesktopConnection open(Device device, boolean tunnelForward,Socket socket) throws IOException {

		Log.i("StartActivity","come in open()....");
			if(socket ==null){
				throw new IOException();
			}
		Log.i("StartActivity","link socketB  status : " + socket);
//		}
		DesktopConnection connection = new DesktopConnection(socket);
		Size videoSize = device.getScreenInfo().getVideoSize();
		connection.send(Device.getDeviceName(), videoSize.getWidth(), videoSize.getHeight());
		return connection;
	}

	public void close() throws IOException {
		socket.shutdownInput();
		socket.shutdownOutput();
		socket.close();
		System.out.println("close()()()");
	}

	@SuppressWarnings("checkstyle:MagicNumber")
	private void send(String deviceName, int width, int height) throws IOException {
		byte[] buffer = new byte[DEVICE_NAME_FIELD_LENGTH + 4];

		byte[] deviceNameBytes = deviceName.getBytes(StandardCharsets.UTF_8);
		int len = Math.min(DEVICE_NAME_FIELD_LENGTH - 1, deviceNameBytes.length);
		System.arraycopy(deviceNameBytes, 0, buffer, 0, len);
		// byte[] are always 0-initialized in java, no need to set '\0' explicitly
		buffer[DEVICE_NAME_FIELD_LENGTH] = (byte) (width >> 8);
		buffer[DEVICE_NAME_FIELD_LENGTH + 1] = (byte) width;
		buffer[DEVICE_NAME_FIELD_LENGTH + 2] = (byte) (height >> 8);
		buffer[DEVICE_NAME_FIELD_LENGTH + 3] = (byte) height;
//		IO.writeFully(fd, buffer, 0, buffer.length);
	}

	public FileDescriptor getFd() {
		return fd;
	}

	public ControlEvent receiveControlEvent() throws IOException {
		ControlEvent event = reader.next();
		while (event == null) {
			reader.readFrom(inputStream);
			event = reader.next();
		}
		return event;
	}
}

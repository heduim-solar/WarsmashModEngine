package com.etheller.warsmash.viewer5;

public class AudioContext {
	private boolean running = false;
	public Listener listener = new Listener();
	public AudioDestination destination = new AudioDestination() {
	};

	public void suspend() {
		this.running = false;
	}

	public boolean isRunning() {
		return this.running;
	}

	public void resume() {
		this.running = true;
	}

	public static class Listener {
		private float x;
		private float y;
		private float z;
		private float forwardX;
		private float forwardY;
		private float forwardZ;
		private float upX;
		private float upY;
		private float upZ;

		public void setPosition(final float x, final float y, final float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public void setOrientation(final float forwardX, final float forwardY, final float forwardZ, final float upX,
				final float upY, final float upZ) {
			this.forwardX = forwardX;
			this.forwardY = forwardY;
			this.forwardZ = forwardZ;
			this.upX = upX;
			this.upY = upY;
			this.upZ = upZ;

		}
	}

	public AudioPanner createPanner() {
		return new AudioPanner() {
			@Override
			public void setPosition(final float x, final float y, final float z) {
				System.err.println("audio panner set position not implemented");
			}

			@Override
			public void connect(final AudioDestination destination) {
				System.err.println("audio panner connect dest not implemented");
			}
		};
	}

	public AudioBufferSource createBufferSource() {
		return new AudioBufferSource();
	}
}

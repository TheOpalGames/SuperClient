package net.theopalgames.superclient;

public interface SuperClientCallback<T> {
	public abstract void done(T result, Throwable error) throws Exception;
}

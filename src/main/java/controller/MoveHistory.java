package controller;

import java.io.*;
import java.util.*;
import model.Game;

class MoveHistory implements Serializable {
	private static final long serialVersionUID = 9178733234692816380L;
	private Deque<byte[]> moveStack = new LinkedList<>(); // holds serialized
															// Games

	void push(Game gameState) {
		try (ByteArrayOutputStream byteArrStream = new ByteArrayOutputStream();
				ObjectOutputStream objOutStream = new ObjectOutputStream(byteArrStream)) {
			objOutStream.writeObject(gameState);
			moveStack.addFirst(byteArrStream.toByteArray());
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	Game pop() {
		if (moveStack.size() < 2)
			throw new NoSuchElementException();
		moveStack.removeFirst();
		byte[] arr = moveStack.peekFirst();
		try (ByteArrayInputStream byteArrStream = new ByteArrayInputStream(arr);
				ObjectInputStream objIn = new ObjectInputStream(byteArrStream)) {
			return (Game) objIn.readObject();
		} catch (StreamCorruptedException s) {
			System.out.println("File corrupted");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}

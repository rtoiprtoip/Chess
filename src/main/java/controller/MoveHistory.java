package controller;

import java.io.*;
import java.util.*;

import lombok.Getter;
import model.Game;

class MoveHistory implements Serializable {

	private static final long serialVersionUID = -2927187439077219768L;

	// holds serialized Games
	private Deque<byte[]> gameStateStack = new LinkedList<>();

	@Getter
	private LinkedList<String> moveLog = new LinkedList<>();

	MoveHistory(Game game) {
		push(game, null, null);
		moveLog.removeLast();
	}

	Game pop() {
		if (gameStateStack.size() < 2)
			throw new NoSuchElementException();
		else {
			gameStateStack.removeLast();
			moveLog.removeLast();
			byte[] arr = gameStateStack.peekLast();
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
		}
		return null;
	}

	void push(Game gameState, Coordinates moveFrom, Coordinates moveTo) {
		push(gameState, moveFrom, moveTo, null);
	}

	void push(Game gameState, Coordinates moveFrom, Coordinates moveTo, String promotionChoice) {
		try (ByteArrayOutputStream byteArrStream = new ByteArrayOutputStream();
				ObjectOutputStream objOutStream = new ObjectOutputStream(byteArrStream)) {
			objOutStream.writeObject(gameState);
			gameStateStack.addLast(byteArrStream.toByteArray());
		} catch (IOException i) {
			i.printStackTrace();
		}
		String promotion = promotionChoice == null ? "" : promotionChoice.split("_")[1];
		moveLog.addLast(moveFrom + "-" + moveTo + " " + promotion);
	}
}

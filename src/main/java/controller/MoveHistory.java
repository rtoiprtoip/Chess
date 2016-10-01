package controller;

import lombok.Getter;
import model.GameLogic;

import java.io.*;
import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;

class MoveHistory implements Serializable {

	private static final long serialVersionUID = -2927187439077219768L;

	// holds serialized Games
	private final Deque<byte[]> gameStateStack = new LinkedList<>();

	@Getter
	private final LinkedList<String> moveLog = new LinkedList<>();

	MoveHistory(GameLogic game) {
		push(game, null, null);
		moveLog.removeLast();
	}

	GameLogic pop() {
		if (gameStateStack.size() < 2)
			throw new NoSuchElementException();
		else {
			gameStateStack.removeLast();
			moveLog.removeLast();
			byte[] arr = gameStateStack.peekLast();
			try (ByteArrayInputStream byteArrStream = new ByteArrayInputStream(arr);
					ObjectInputStream objIn = new ObjectInputStream(byteArrStream)) {
				return (GameLogic) objIn.readObject();
			} catch (StreamCorruptedException s) {
				System.out.println("File corrupted");
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	void push(GameLogic gameState, Coordinates moveFrom, Coordinates moveTo) {
		push(gameState, moveFrom, moveTo, null);
	}

	void push(GameLogic gameState, Coordinates moveFrom, Coordinates moveTo, String promotionChoice) {
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

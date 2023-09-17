package net.natte.bankstorage.rendering;

public class ItemCountUtils {

	private static final char[] POWER = {
			'K',
			'M',
			'B',
			'T'
	};

	public static String toConsiseString(int count) {
		int index = 0;
		if (count > 9999) {
			while (count / 1000 != 0) {
				count /= 1000;
				index++;
			}
		}

		if (index > 0) {
			return count + String.valueOf(POWER[index - 1]);
		} else {
			return String.valueOf(count);
		}
	}

	/**
	 * How big or small to render the item count text
	 * @return 1 == vanilla
	 */
	public static float scale(String string) {
		if (string.length() > 3) {
			return 5f/8f;//0.5f;
		} else if (string.length() == 3) {
			return 6f/8f;
		}
		return 1f;
	}

}
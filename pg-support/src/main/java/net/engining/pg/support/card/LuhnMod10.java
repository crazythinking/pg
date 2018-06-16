package net.engining.pg.support.card;

/**
 * The Luhn Mod-10 Method (ISO 2894/ANSI 4.13) involves a check digit in the
 * one's position.  The check digit is calculated starting at the right with
 * the digit immediately preceding the check digit (ten's digit) and moving
 * toward the left, doubling every other digit.  If a doubled digit is greater
 * than nine, the two digits are added to together to obtain a single-digit
 * result.  The sum of all the resulting digits (including those skipped) is then
 * taken modulo with 10, to obtain the check digit.
 * 
 * @author licj
 *
 */
public abstract class LuhnMod10
{
    private static final int tableLuhnMod10[][] = {{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, {0, 2, 4, 6, 8, 1, 3, 5, 7, 9}};

    /**
     * @param cardNumber 不带校验位的卡号
     * @return 卡号校验位
     */
    public static char getDigit(String cardNumber) {
        char cn[] = cardNumber.toCharArray();
        int odd = 0;
        int sum = 0;
        for (int i = cn.length - 1; i >= 0; i--)
            if (Character.isDigit(cn[i]))
                sum += tableLuhnMod10[(odd = 1 - odd)][cn[i] - '0'];
        sum %= 10;
        return (char)(0x30 + (sum > 0 ? 10 - sum : 0)); /* return the check digit */
    }
}

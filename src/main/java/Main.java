import java.math.BigInteger;
import java.util.Map;

class Scratch {
    public static void main(String[] args) {
        final var valueLong = 218073997501L;
        final var optInverse = GenericUtils.inverse_mod_prime_power(valueLong, 103, 3);
        final var modInverse = BigInteger.valueOf(valueLong).modInverse(BigInteger.valueOf(103).pow(3));
        Runner.run();
    }
}

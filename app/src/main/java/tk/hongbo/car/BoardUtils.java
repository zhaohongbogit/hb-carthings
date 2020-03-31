package tk.hongbo.car;

import android.os.Build;

import com.google.android.things.contrib.driver.pwmservo.Servo;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;

import java.io.IOException;

public class BoardUtils {

    private interface BoardDefaults {
        String getServoBus(); //获取前轮转向舵机端口

        String getServoBusEn1(); //获取电机使能1端口

        String getServoBusEn2(); //获取电机使能2端口

        String getServoBusEnA(); //获取电机使能动力端口
    }

    private static final class Rpi3BoardDefaults implements BoardDefaults {

        @Override
        public String getServoBus() {
            return "PWM0";
        }

        @Override
        public String getServoBusEn1() {
            return "BCM6";
        }

        @Override
        public String getServoBusEn2() {
            return "BCM5";
        }

        @Override
        public String getServoBusEnA() {
            return "PWM1";
        }
    }

    private static final class Imx7BoardDefaults implements BoardDefaults {

        @Override
        public String getServoBus() {
            return "PWM1";
        }

        @Override
        public String getServoBusEn1() {
            return "GPIO2_IO01";
        }

        @Override
        public String getServoBusEn2() {
            return "GPIO2_IO02";
        }

        @Override
        public String getServoBusEnA() {
            return "PWM2";
        }
    }

    private static final BoardDefaults BOARD = Build.DEVICE.equals("rpi3") ?
            new Rpi3BoardDefaults() : new Imx7BoardDefaults();

    public static Gpio openGpio(String pin) throws IOException {
        PeripheralManager peripheralManager = PeripheralManager.getInstance();
        Gpio gpio = peripheralManager.openGpio(pin);
        gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        gpio.setActiveType(Gpio.ACTIVE_HIGH);
        gpio.setValue(false);
        return gpio;
    }

    public static Gpio openEn1() throws IOException {
        return openGpio(BOARD.getServoBusEn1());
    }

    public static Gpio openEn2() throws IOException {
        return openGpio(BOARD.getServoBusEn2());
    }

    public static Pwm openPwn() throws IOException {
        PeripheralManager peripheralManager = PeripheralManager.getInstance();
        Pwm motorENA = peripheralManager.openPwm(BOARD.getServoBusEnA());
        motorENA.setPwmFrequencyHz(50);
        motorENA.setPwmDutyCycle(100);
        return motorENA;
    }

    public static Servo openPwnYun() throws IOException {
        return new Servo(BOARD.getServoBus());
    }
}

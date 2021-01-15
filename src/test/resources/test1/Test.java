
public class Test {

    private final int a;
    private int b;
    private final Test test;

    public Test(String[] args) {
        this.a = Integer.parseInt(args[0]);
        b = 2;
        if (b == 1) {
            test = new Test(args);
            test.b = 3;
        } else {
            test = null;
        }
    }

    public static void doIt(int a) {
        if (a == 1) {
            System.out.println(1);
        } else if (a == 2) {
            System.out.println(2);
        } else if (a == 1) {
            System.out.println(3);
        } else {
            System.out.println(100);
        }
    }

    public static void main(String[] args) {
        int a = 123;
        a = Integer.parse(args[0]);
        if (a == 1) {
            System.out.println(1);
        } else if (a == 2) {
            System.out.println(2);
        } else if (a == 1) {
            System.out.println(3);
        } else {
            System.out.println(100);
        }

        Test var = new Test(args);
    }
}

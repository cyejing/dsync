package cn.cyejing.dsync.toolkit;

/**
 * @Description:
 * @Author: Born
 * @Create: 2018-10-17 16:50
 **/
public class Main {

    public static void main(String[] args) throws Exception {
        DLock.getInstance().lock("asd");
        System.out.println("getLock");
        DLock.getInstance().unlock();
        System.out.println("getLock2");
        DLock.getInstance().lock("asd");


    }


}

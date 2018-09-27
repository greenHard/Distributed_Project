package com.zhang.zookeeper.sence.namingService;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.sence.namingService.IdMakerTest
 * @Description: 唯一id生成测试
 * @create 2018/09/26 16:41
 */
public class IdMakerTest {
    public static void main(String[] args) throws Exception {
        IdMaker idMaker = new IdMaker();
        idMaker.start();
        try {
            for (int i = 0; i < 10; i++) {
                String id = idMaker.generateId(IdMaker.RemoveMethod.DELAY);
                System.out.println(id);
            }
        } finally {
            idMaker.stop();
        }
    }
}

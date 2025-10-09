//package product_service.cronjob;
//
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Component
//public class Test {
//    @Scheduled(fixedRate = 5000)
//    public void cacheUpdater() {
//        try{
//            Thread.sleep(2000);
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//        System.out.println("[CacheUpdaterJob] Cron is running... " + System.currentTimeMillis());
//    }
//
//    @Scheduled(fixedDelay = 5000)
//    public void runFixedDelay() {
//        System.out.println("🕒 fixedDelay: " + System.currentTimeMillis());
//    }
//
//    @Scheduled(cron = "*/10 * * * * *")
//    public void runCron() {
//        System.out.println("⏰ cron: chạy mỗi 10 giây");
//    }
//}
//

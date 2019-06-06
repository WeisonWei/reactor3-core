package com.reactor.mono;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class MonoOperators {
    public static void main(String[] args) throws InterruptedException {
        Mono.just(1).mergeWith(Flux.just(1, 2, 3)).subscribe(System.out::print);
        System.out.println();
        Mono.fromSupplier(() -> "Hello").subscribe(System.out::println);
        Mono.justOrEmpty(Optional.of("Hello")).subscribe(System.out::println);
        Mono.create(sink -> sink.success("Hello")).subscribe(System.out::println);
        System.out.println("---------- 分割线1 ----------");

        // zip 将多个Mono合并成一个Mono
        Mono.zip(string -> string.length, Mono.just(1), Mono.just(2))
                .subscribe(System.out::println);
        System.out.println("---------- 分割线2 ----------");

        // then 当订阅成功或失败后 返回另外一个publisher
        Mono.zip(string -> string.length, Mono.just(1))
                .map(getIntegerIntegerFunction())
                .doOnSuccess(integer -> System.out.println("成功了"))
                .doOnTerminate((a, e) -> System.out.println("结束了"))
                .then(() -> Mono.just(888))
                .subscribe(System.out::println);
        System.out.println("---------- 分割线3 ----------");

        //冷/懒加载 当deferMono被订阅时，才会触发1的feeService()
        Mono.defer(() -> Mono.just(feeService())) // 1
                .map(integer -> integer + 1)
                .subscribe(System.out::println);

        //热加载 Mono.just时，直接触发了1的feeService()
        Mono.just(feeService()) //1
                .map(integer -> integer + 1)
                .subscribe(System.out::println);
        System.out.println("---------- 分割线4 ----------");


        Mono.delay(Duration.ofMillis(3)).subscribe(System.out::println);
        TimeUnit.SECONDS.sleep(1);
        System.out.println("---------- 分割线5 ----------");

        // using在订阅者取消或者异常时 能执行3将资源清理
        // true&false暂时没看到区别
        Mono.using(() -> 1, // 1 数据源
                integer -> Mono.just(2 + integer), // 2 最终返回
                integerSource -> System.out.println("清理结果是：" + integerSource), //3 根据4执行3
                false)// 4 完成前调用 还是 完成后调用
                .flatMap(integer -> Mono.just(integer + 3))
                .subscribe(integer -> System.out.println("最终结果是：" + integer));

        Mono.using(() -> 1, // 1 数据源
                integer -> Mono.just(2 + integer), // 2 最终返回
                integerSource -> System.out.println("清理结果是：" + integerSource), //3 根据4执行3
                true)// 4 完成前调用 还是 完成后调用
                .flatMap(integer -> Mono.just(integer + 3))
                .subscribe(integer -> System.out.println("最终结果是：" + integer));

        Mono<Void> when = Mono.when();
        Mono.when(Mono.just(1), Mono.just(2), (m1, m2) -> m1 + m2).subscribe(System.out::println);

        Mono.just(1).concatWith(Mono.just(2)).subscribe(System.out::println);

    }

    private static Integer feeService() {
        //doSomething
        //call db
        //calculate fee
        return 1;
    }

    private static Function<Integer, Integer> getIntegerIntegerFunction() {
        return integer -> {
            integer = integer + 1;
            System.out.println("----->" + integer);
            return integer;
        };
    }
}
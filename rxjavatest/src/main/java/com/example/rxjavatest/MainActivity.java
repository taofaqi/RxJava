package com.example.rxjavatest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.rx2androidnetworking.Rx2AndroidNetworking;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private Button btn1;
    private Button btn2;
    private Button btn3;
    private Button btn4;
    private Button btn5;

    private static final String TAG = "MainActivity";
    private static boolean hasCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1 = ((Button) findViewById(R.id.main_btn1));
        btn2 = ((Button) findViewById(R.id.main_btn2));
        btn3 = ((Button) findViewById(R.id.main_btn3));
        btn4 = ((Button) findViewById(R.id.main_btn4));
        btn5 = ((Button) findViewById(R.id.main_btn5));

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rxJavaTest();
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exChangeThread();

            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestDatas();

            }
        });
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestDatasWithCache();
            }
        });
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestDatas2();
            }
        });
    }

    /*Rxjava操作流程*/
    public void rxJavaTest() {
        Observable.create(new ObservableOnSubscribe<Integer>() { // 第一步：初始化Observable
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                Log.e(TAG, "Observable emit 1" + "\n");
                e.onNext(2);
                Log.e(TAG, "Observable emit 2" + "\n");
                e.onNext(3);
                e.onComplete();
                Log.e(TAG, "Observable emit 3" + "\n");
                e.onNext(4);
                Log.e(TAG, "Observable emit 4" + "\n");
            }
        }).subscribe(new Observer<Integer>() { // 第三步：订阅

            // 第二步：初始化Observer
            private int i;
            private Disposable mDisposable;

            @Override
            public void onSubscribe(@NonNull Disposable d) {   //整个订阅第一次经过这里
                Log.e(TAG, "onSubscribe--" + "\n");
                mDisposable = d;
            }

            @Override
            public void onNext(@NonNull Integer integer) {

                if (integer == 4) {
                    // 在RxJava 2.x 中，新增的Disposable可以做到切断的操作，让Observer观察者不再接收上游事件
                    mDisposable.dispose();
                }
                Log.e(TAG, "onNext:" + integer + "\n");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.e(TAG, "onError : value : " + e.getMessage() + "\n");
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete" + "\n");
            }
        });
    }

    /*RxJava的线程切换*/
    public void exChangeThread() {
        //1.创建被订阅者
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override                   //事件发射器（发送事件）
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                Log.e(TAG, "Observable thread is:" + Thread.currentThread().getName());
                e.onNext(1);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.newThread())  //指定事件发送线程(新线程)
                .subscribeOn(Schedulers.io())   //多次指定发射事件的线程只有第一次指定的有效，其他的被忽略
                .observeOn(AndroidSchedulers.mainThread())  //指定订阅者接收事件的线程
                .doOnNext(new Consumer<Integer>() {  //Consumer 即消费者  BiConsumer 则是接收两个值  Function 用于变换对象，Predicate 用于判断
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {
                        Log.e(TAG, "After observeOn(mainThread),current Thread is:" + Thread.currentThread().getName());
                    }
                })
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {
                        Log.e(TAG, "After observeOn(IO),current Thread is:" + Thread.currentThread().getName());
                    }
                })
                .subscribe(new Observer<Integer>() {   //2.订阅事件
                    //创建Observer  订阅者
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull Integer integer) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /*RxJava配合OkHttp的网络请求   map 操作符可以将一个 Observable 对象通过某种关系转换为另一个Observable 对象。*/
    public void requestDatas() {
        //1.创建被Observable观察者
        //1.通过 Observable.create() 方法，调用 OkHttp 网络请求；
        Observable.create(new ObservableOnSubscribe<Response>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Response> e) throws Exception {
                //创建请求
                Request.Builder builder = new Request.Builder()
                        .url("https://zhuanlan.zhihu.com/api/columns/qinchao")
                        .get();
                Request request = builder.build();
                Call call = new OkHttpClient().newCall(request);
                Response response = call.execute();
                e.onNext(response);
            }
            /*
            * map 操作符可以将一个 Observable 对象通过某种关系转换为另一个Observable 对象。
            * */
        }).map(new Function<Response, MobileAddress>() {  //2.通过 map 操作符集合 gson，将 Response 转换为 bean 类；
            @Override
            public MobileAddress apply(@NonNull Response response) throws Exception {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        Log.e(TAG, "map:转换前：" + body);
                        return getMobileAddress();//new Gson().fromJson(body.toString(), MobileAddress.class);
                    }
                }
                return null;
            }
        }).observeOn(AndroidSchedulers.mainThread())  //4.调度线程，在子线程中进行耗时操作任务，在主线程中更新 UI ；
                //3.通过 doOnNext() 方法，解析 bean 中的数据，并进行数据库存储等操作；
                .doOnNext(new Consumer<MobileAddress>() {//指定订阅者接收事件的线程
                    @Override
                    public void accept(@NonNull MobileAddress mobileAddress) throws Exception {
                        Log.e(TAG, "doOnNext:解析成功：" + mobileAddress.toString() + "\n");
                    }
                })
                .subscribeOn(Schedulers.io())  //指定被订阅者发射事件的线程
                .observeOn(AndroidSchedulers.mainThread())//指定订阅者接收事件的线程
                //5.通过 subscribe()，根据请求成功或者失败来更新 UI
                .subscribe(new Consumer<MobileAddress>() {
                    @Override
                    public void accept(@NonNull MobileAddress mobileAddress) throws Exception {
                        Log.e(TAG, "成功:" + mobileAddress.toString() + "\n");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Log.e(TAG, "失败:" + throwable.getMessage() + "\n");
                    }
                });
    }

    /*RxJava concat 操作符先读取缓存再通过网络请求获取数据
    * concat 的必须调用 onComplete 后才能订阅下一个 Observable
    * 先读取缓存数据，倘若获取到的缓存数据不是我们想要的，再调用 onComplete() 以执行获取网络数据的 Observable，
    * 如果缓存数据能应我们所需，则直接调用 onNext()，防止过度的网络请求，浪费用户的流量*/
    public void requestDatasWithCache() {
        Observable<MobileAddress> cache = Observable.create(new ObservableOnSubscribe<MobileAddress>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<MobileAddress> e) throws Exception {
                Log.e(TAG, "creata当前线程:" + Thread.currentThread().getName());

                // 在操作符 concat 中，只有调用 onComplete 之后才会执行下一个 Observable
                if (hasCache) {   //如果缓存不为空，先拉取缓存数据，不去请求网络接口
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "subscribe : " + "读取缓存数据");
                        }
                    });
                    e.onNext(getMobileAddress());
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "subscribe : " + "读取网络数据");
                        }
                    });
                    e.onComplete();
                }
                hasCache = !hasCache;
            }
        });

        Observable<MobileAddress> network = Rx2AndroidNetworking.get("https://zhuanlan.zhihu.com/api/columns/qinchao")
                .build()
                .getObjectObservable(MobileAddress.class);

        //两个Observable的泛型应当保持一种
        Observable.concat(cache, network)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<MobileAddress>() {
                    @Override
                    public void accept(@NonNull MobileAddress mobileAddress) throws Exception {
                        Log.e(TAG, "subscribe 成功:" + Thread.currentThread().getName());
                        if (!hasCache) {
                            Log.e(TAG, "accept : 网络获取数据设置缓存: \n" + mobileAddress.toString());
                        }
                        Log.e(TAG, "accept: 读取数据成功:" + mobileAddress.toString());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Log.e(TAG, "subscribe 失败:" + Thread.currentThread().getName());
                        Log.e(TAG, "accept: 读取数据失败：" + throwable.getMessage());
                    }
                });
    }

    /*RxJava操作符  flatMap 实现多个网络请求依次依赖*/
    public void requestDatas2() {
        Rx2AndroidNetworking.get("https://zhuanlan.zhihu.com/api/columns/qinchao")
                .build()
                .getObjectObservable(MobileAddress.class)   //发起请求，并解析到MobileAddress
                .subscribeOn(Schedulers.io())  //在IO线程中进行网络请求
                .observeOn(AndroidSchedulers.mainThread())  //在主线程处理请求结果
                .doOnNext(new Consumer<MobileAddress>() {
                    @Override
                    public void accept(@NonNull MobileAddress mobileAddress) throws Exception {
                        //先根据请求结果做一些操作
                        Log.e(TAG, "第一次请求网络。。。accept: doOnNext:" + "操作请求结果");
                    }
                })
                .observeOn(Schedulers.io()) // 回到IO线程去处理详情的请求
                .flatMap(new Function<MobileAddress, ObservableSource<MobileAddressDetail>>() {

                    @Override
                    public ObservableSource<MobileAddressDetail> apply(@NonNull MobileAddress mobileAddress) throws Exception {
//                        return Rx2AndroidNetworking.post("http://www.tngou.net/api/food/show")
//                        .addBodyParameter("id", foodList.getTngou().get(0).getId() + "")
                        return Rx2AndroidNetworking.get("https://zhuanlan.zhihu.com/api/columns/qinchao")
                                .build()
                                .getObjectObservable(MobileAddressDetail.class);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<MobileAddressDetail>() {
                    @Override
                    public void accept(@NonNull MobileAddressDetail mobileAddressDetail) throws Exception {
                        MobileAddressDetail mobileAddressDetail1 = getMobileAddressDetail();
                        Log.e(TAG, "第二次请求网络。。。accept: success ：" + mobileAddressDetail1.toString());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Log.e(TAG, "第二次请求网络。。。accept: error ：" + throwable.getMessage());
                    }
                });
    }

    public MobileAddress getMobileAddress() {
        MobileAddress mobileAddress = new MobileAddress();
        mobileAddress.followersCount = 30246;
        mobileAddress.href = "/api/columns/qinchao";
        mobileAddress.acceptSubmission = true;
        mobileAddress.firstTime = false;
        mobileAddress.canManage = false;
        mobileAddress.description = "微信公众号: qc_empire,知乎: http://www.zhihu.com/people/qin.chao";
        mobileAddress.banUntil = 0;
        mobileAddress.slug = "qinchao";
        mobileAddress.name = "跨越美利坚 - 面试、创业、技术培训";
        return mobileAddress;
    }

    public MobileAddressDetail getMobileAddressDetail() {
        MobileAddressDetail mobileAddressDetail = new MobileAddressDetail();
        mobileAddressDetail.detail = "请求数据的详情";
        return mobileAddressDetail;
    }

    /*zip 操作符可以将多个 Observable 的数据结合为一个数据源再发射出去。

    Observable<MobileAddress> observable1 = Rx2AndroidNetworking.get("http://api.avatardata.cn/MobilePlace/LookUp?key=ec47b85086be4dc8b5d941f5abd37a4e&mobileNumber=13021671512")
            .build()
            .getObjectObservable(MobileAddress.class);

    Observable<CategoryResult> observable2 = Network.getGankApi()
            .getCategoryData("Android",1,1);

    Observable.zip(observable1, observable2, new BiFunction<MobileAddress, CategoryResult, String>() {
        @Override
        public String apply(@NonNull MobileAddress mobileAddress, @NonNull CategoryResult categoryResult) throws Exception {
            return "合并后的数据为：手机归属地："+mobileAddress.getResult().getMobilearea()+"人名："+categoryResult.results.get(0).who;
        }
    }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<String>() {
        @Override
        public void accept(@NonNull String s) throws Exception {
            Log.e(TAG, "accept: 成功：" + s+"\n");
        }
    }, new Consumer<Throwable>() {
        @Override
        public void accept(@NonNull Throwable throwable) throws Exception {
            Log.e(TAG, "accept: 失败：" + throwable+"\n");
        }
    });*/
/*
    采用 interval 操作符实现心跳间隔任务

    想必即时通讯等需要轮训的任务在如今的 APP 中已是很常见，而 RxJava 2.x 的 interval 操作符可谓完美地解决了我们的疑惑。

    这里就简单的意思一下轮训。

    private Disposable mDisposable;
    @Override
    protected void doSomething() {
        mDisposable = Flowable.interval(1, TimeUnit.SECONDS)
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        Log.e(TAG, "accept: doOnNext : "+aLong );
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        Log.e(TAG, "accept: 设置文本 ："+aLong );
                        mRxOperatorsText.append("accept: 设置文本 ："+aLong +"\n");
                    }
                });
    }

    *//**
     * 销毁时停止心跳
     *//*
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDisposable != null){
            mDisposable.dispose();
        }
    }*/

}
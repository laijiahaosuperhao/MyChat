package com.example.laijiahao.mychat.utils;

/**
 * Created by laijiahao on 16/10/26.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 图片加载类
 */
public class ImageLoader {

    //编写单例代码，首先声明一个ImageLoader对象
    private static ImageLoader mInstance;

    //1.图片缓存的核心对象 key:图片路径 value:Bitmap;;用来管理我们图片所占据的内存
    private LruCache<String, Bitmap> mLruCache;

    //2.线程池，去执行我们加载图片的任务
    private ExecutorService mThreadPool;

    //3.线程池有一个常量，作为它的默认线程数
    private static final int DEAFULT_THREAD_COUNT = 1;

    //5.记录当前队列的调度方式
    private Type mType = Type.LIFO;

    //6.任务队列 TaskQueue 用LinkedList实现，供线程池去取任务
    private LinkedList<Runnable> mTaskQueue;

    //7.后台轮询线程
    private Thread mPoolThread;
    //8.跟这个线程绑定的有一个handler，专门用于给这个线程中的MessageQueue发送消息
    private Handler mPoolThreadHandler;

    //9.还有一个handler用于分析图片，是UI线程中的handler，用于传入一个Path以后，
    // 当我们图片获取成功以后，会通过mUIHandler发送消息，为图片设置回调，回调显示它的Bitmap
    //我们在getView里面，就会使用一句方法，ImageLoader.getInstance().loadImage(path)就结束了，并不需要
    //在getView里面再写回调方法，我们都会在这个类里面内部实现，实现的核心就要用到这个mUIHandler
    private Handler mUIHandler;

    //信号量，默认是0个,用于异步线程之间的一个顺序的执行，保证mPoolThreadHandler使用的时候不等于null,防止报空指针异常
    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);
    //为了让线程池可以真正做到LIFO
    private Semaphore mSemaphoreThreadPool;

    //4.线程有一个调度方式，就是队列，线程池去取task，有一个调度方式，对应图片的加载策略FIFO,LIFO,所以有一个枚举类型
    public enum Type {
        FIFO, LIFO;
    }

    //私有化构造方法,这样外界无法用new进行构造，在构造方法里面进行一定的初始化
    //传入用户可以控制它的线程数即后台的线程数，三个或四个线程去加载显示图片；Type也让用户进行指定
    private ImageLoader(int threadCount, Type type) {
        //初始化操作
        init(threadCount, type);
    }

    private void init(int threadCount, Type type) {
        //1.初始化后台轮询线程
        mPoolThread = new Thread() {
            @Override
            public void run() {
                super.run();
                //创建Looper对象，去准备MessageQueue等等
                Looper.prepare();
                mPoolThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        //让后台的线程池去取出一个任务进行执行
                        mThreadPool.execute(getTask());

                        try {
                            mSemaphoreThreadPool.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                //释放一个信号量
                mSemaphorePoolThreadHandler.release();
                //循环地处理消息,在后台不断的轮询
                Looper.loop();
            }
        };

        mPoolThread.start();

        //2.初始化LruCache，定义它所包含的内存，首先获取应用最大的可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            //测量每个bitmap的值
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //return每个bitmap的值所占据的内存  每一行占据的字节数＊它的高度
                return value.getRowBytes() * value.getHeight();//

            }
        };

        //3.创建线程池
        mThreadPool = Executors.newFixedThreadPool(threadCount);
        //4.创建任务队列
        mTaskQueue = new LinkedList<Runnable>();
        //5.指定队列的调度方式
        mType = type;
        //初始化信号量
        mSemaphoreThreadPool = new Semaphore(threadCount);

    }

    /**
     * 从任务队列取出一个方法
     *
     * @return
     */
    private Runnable getTask() {
        if (mType == Type.FIFO) {
            return mTaskQueue.removeFirst();
        } else if (mType == Type.LIFO) {
            return mTaskQueue.removeLast();
        }
        return null;
    }

    //编写getInstance方法,因为我们没有实例，我们需要类.方法名去调用，所以编写getInstance方法必须是static类型
    public static ImageLoader getInstance(int threadCount,Type type) {
        //使用懒加载,两层判断
        if (mInstance == null) {
            //使用同步
            synchronized (ImageLoader.class) {
                //进行二次判断
                if (mInstance == null) {
                    mInstance = new ImageLoader(threadCount, type );
                }
            }
        }
        return mInstance;
    }

    /**
     * 根据path为imageview设置图片
     * <p>
     * loadImage方法运行在UI线程，mUIHandler可以去操作imageView，为imageView设置bitmap
     *
     * @param path
     * @param imageView
     */
    //根据path去加载图片，需要把bitmap放在ImageView上
    public void loadImage(final String path, final ImageView imageView) {
        //防止调用多次imageView复用之后造成混乱，有必要设置一个path。图片加载完以后，会根据Tag去对比path
        imageView.setTag(path);

        //初始化UIHandler
        if (mUIHandler == null) {
            mUIHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    //获取得到图片，异步加载以后，拿到bitmap，为imageview回调设置图片
                    ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
                    Bitmap bm = holder.bitmap;
                    ImageView imageView = holder.imageView;
                    String path = holder.path;
                    //将path与getTag存储路径进行比较,因为imageView 在GridView当中还是复用的，
                    // 有可能一开始imageView是显示第一张图片，传入的是第一张图片的path,setTag(path)也是第一张图片的path
                    //当划到第二屏的时候，此时imageView这个对象没有变，但是它的path，setTag已经变了
                    //而第一张图片加载完，必须用if()进行比对，如果不比对的话，imageView.setImageBitmap(bm)显示的是第一张
                    //图片的内容，但是目前已经到第二屏了，应该显示第二屏第一张图片的内容，所以我们使用Tag绑定它的path进行比对
                    //因为滑动到第二屏，它的path肯定是变化了，imageView对象虽然没有变化，但是setTag已经发生了变化，所以说我们的path
                    // 和它的tag一定要一致，这样去显示就不会造成图片设置的错乱
                    if (imageView.getTag().toString().equals(path)) {
                        imageView.setImageBitmap(bm);
                    }

                }
            };
        }

        //根据path在缓存中获取bitmap
        Bitmap bm = getBitmapFromLruCache(path);

        if (bm != null) {
            refreshBitmap(bm, path, imageView);
        } else {
            addTask(new Runnable() {
                @Override
                public void run() {
                    //加载图片
                    //图片的压缩
                    //1、获得图片需要显示的大小
                    ImageSize imageSize = getImageViewSize(imageView);
                    //2.压缩图片
                    Bitmap bm = decodeSampledBitmapFromPath(path, imageSize.width, imageSize.height);
                    //3.把图片加入到缓存,需传入path，因为key是path
                    addBitmapToLruCache(path, bm);

                    refreshBitmap(bm, path, imageView);

                    mSemaphoreThreadPool.release();
                }
            });
        }
    }

    private void refreshBitmap(Bitmap bm, String path, ImageView imageView) {
        Message message = Message.obtain();
        ImgBeanHolder holder = new ImgBeanHolder();
        holder.bitmap = bm;
        holder.path = path;
        holder.imageView = imageView;
        message.obj = holder;
        mUIHandler.sendMessage(message);
    }

    /**
     * 将图片加入到缓存
     *
     * @param path
     * @param bm
     */
    private void addBitmapToLruCache(String path, Bitmap bm) {
        if (getBitmapFromLruCache(path) == null) {
            if(bm!=null) {
                mLruCache.put(path, bm);
            }
        }
    }

    /**
     * 根据图片需要显示的宽和高对图片进行压缩
     *
     * @param path
     * @param width
     * @param height
     * @return
     */
    private Bitmap decodeSampledBitmapFromPath(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //设置为true的目的，获取图片的宽和高，并不把图片加载到内存中
        options.inJustDecodeBounds = true;
        //options获得了图片的实际的宽和高，根据实际的宽和高，和需求的宽和高作对比，
        // 然后获得到一个InSampledSize,然后根据SampledSize去压缩我们的图片
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = caculateInSampleSize(options, width, height);

        //使用获取到的InSampleSize再次解析图片，这里options.inJustDecodeBounds设置为false，
        // 因为要把图片加载到内存了。
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        return bitmap;
    }

    /**
     * 根据需求的宽和高以及图片实际的宽和高计算SampleSize
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private int caculateInSampleSize(BitmapFactory.Options options, int
            reqWidth, int reqHeight) {
        //通过options拿到实际宽度和实际高度
        int width = options.outWidth;
        int height = options.outHeight;

        int inSampleSize = 1;
        if (width > reqWidth || height > reqHeight) {
            //求得比例，使用Math.round四舍五入，除之前转成float类型，不然它直接取整。
            int widthRadio = Math.round(width * 1.0f / reqWidth);
            int heightRadio = Math.round(height * 1.0f / reqHeight);
            //比如实际的宽width为300，高height为500，而我们需求reqWidth，reqHeight都为100，取比例最大值为5
            //一般，为了图片不失真，保持原比例的话，比例取小值，这样得到的图片会比显示器大一点；这里为了节省内存，比例设置为最大值
            inSampleSize = Math.max(widthRadio, heightRadio);
        }
        return inSampleSize;
    }


    //需要返回值，返回图片需要的宽和高,但是这里返回值只能写一个int，所以我们需要一个Link对象,ImageSize

    /**
     * 根据ImageView获取适当的压缩的宽和高
     *
     * @param imageView
     * @return
     */
    protected ImageSize getImageViewSize(ImageView imageView) {

        ImageSize imageSize = new ImageSize();

        DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();

        ViewGroup.LayoutParams lp = imageView.getLayoutParams();

        //获取imageview的实际宽度，有可能imageview刚new出来还没有添加到它的容器当中种种原因get宽度为0
        int width = imageView.getWidth();

        if (width <= 0) {
            //获取imageview在layout中声明的宽度，当然它有可能声明为WRAP_CONTENT为-2，和MATCH_PARENT为-1
            width = lp.width;
        }

        if (width <= 0) {
            //width = imageView.getMaxWidth();//检查最大值，API为16才有的方法
            width = getImageViewFieldValue(imageView,"mMaxWidth");//检查最大值
        }

        if (width <= 0) {
            width = displayMetrics.widthPixels;//获取屏幕宽度
        }


        int height = imageView.getHeight();

        if (height <= 0) {
            height = lp.height;
        }

        if (height <= 0) {
            //height = imageView.getMaxHeight();//检查最大值，API为16才有的方法
            height = getImageViewFieldValue(imageView,"mMaxHeight");//检查最大值
        }

        if (height <= 0) {
            height = displayMetrics.heightPixels;//获取屏幕高度
        }

        imageSize.width = width;
        imageSize.height = height;

        return imageSize;
    }

    //通过反射获取ImageView某一个属性值，传入当前的对象，传入需要的属性值
    private static int getImageViewFieldValue(Object object,String fieldName){
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);

            int fieldValue = field.getInt(object);
            if(fieldValue>0 && fieldValue<Integer.MAX_VALUE){
                value = fieldValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    private class ImageSize {
        int width;
        int height;
    }

    private synchronized void addTask(Runnable runnable) {
        //创建一个Task并放在TaskQueue当中 且会发送一个通知去提醒后台线程
        mTaskQueue.add(runnable);

        try {
            if (mPoolThreadHandler == null)
                mSemaphorePoolThreadHandler.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mPoolThreadHandler.sendEmptyMessage(0x110);

    }

    private Bitmap getBitmapFromLruCache(String key) {
        return mLruCache.get(key);
    }

    //用来持有bitmap
    private class ImgBeanHolder {
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }
}

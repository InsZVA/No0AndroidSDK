短学期项目，给安卓写的一个SDK，用来发HTTP请求。
线程是乱开的，请求都是阻塞API，没有用NAPI，唯一就是用Handler做了个Callback机制。
加了后台Service，用来长连接接受服务器推送。
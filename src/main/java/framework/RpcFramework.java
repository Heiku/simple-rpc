package framework;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * study rpc framework from alibaba liangfei
 *
 * @Author: Heiku
 * @Date: 2019/9/20
 */
public class RpcFramework {


    /**
     * 服务暴露
     *
     * @param service 服务实现
     * @param port    服务端口
     * @throws Exception 异常
     */
    public static void export(final Object service, int port) throws Exception {
        if (service == null) {
            throw new IllegalArgumentException("service instance is null");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port " + port);
        }

        // start export
        System.out.println("Export Service " + service.getClass().getName() + " on port " + port);

        ServerSocket server = new ServerSocket(port);
        for (; ; ) {
            try {
                final Socket socket = server.accept();
                new Thread(() -> {
                    try {
                        try {
                            // 读取对象字节流
                            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                            try {
                                // 获取方法名，方法参数类型
                                String methodName = input.readUTF();
                                Class<?>[] parameterTypes = (Class<?>[]) input.readObject();

                                // 获取参数数据
                                Object[] arguments = (Object[]) input.readObject();

                                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                                try {
                                    Method method = service.getClass().getMethod(methodName, parameterTypes);
                                    Object result = method.invoke(service, arguments);

                                    // 结果写回
                                    outputStream.writeObject(result);
                                } catch (Throwable t) {
                                    outputStream.writeObject(t);
                                } finally {
                                    outputStream.close();
                                }
                            } finally {
                                input.close();
                            }
                        } finally {
                            socket.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 调用服务
     *
     * @param interfaceClass 接口类型
     * @param host           服务端主机名
     * @param port           服务端端口
     * @param <T>            接口泛型
     * @throws Exception Exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T refer(final Class<T> interfaceClass, final String host, final int port) throws Exception {
        if (interfaceClass == null) {
            throw new IllegalArgumentException("Interface class is null");
        }
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("The " + interfaceClass.getName() + " must be interface class");
        }
        if (host == null || host.length() == 0) {
            throw new IllegalArgumentException("Host is null");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port " + port);
        }

        System.out.println("Get remote service " + interfaceClass.getName() + " from server " + host + " : " + port);

        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Socket socket = new Socket(host, port);
                try {
                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

                    try {
                        output.writeUTF(method.getName());
                        output.writeObject(method.getParameterTypes());
                        output.writeObject(args);

                        ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                        try {
                            Object result = input.readObject();
                            if (result instanceof Throwable) {
                                throw (Throwable) result;
                            }
                            return result;
                        } finally {
                            input.close();
                        }
                    } finally {
                        output.close();
                    }
                } finally {
                    socket.close();
                }
            }
        });
    }
}

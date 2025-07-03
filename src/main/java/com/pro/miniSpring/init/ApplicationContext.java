package com.pro.miniSpring.init;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ApplicationContext {

    private static final Logger log = LoggerFactory.getLogger(ApplicationContext.class);
    /**
     * 存储bean名称到对象的映射
     */
    private static final Map<String, Object> beans = new HashMap<String, Object>();
    /**
     * 存储bean名称到bean类型的映射
     */
    private static final Map<String, Class<?>> beanDefinitions = new HashMap<>();

    /**
     * bean类型到名称的映射【用于autowired注解的字段依赖注入时使用】
     */
    private static final Map<Class<?>, String> typeToBeanName = new HashMap<>();
    /**
     * 接口和对应实现类名称的映射
     */
    private static final Map<Class<?>,List<String>> classToImplNames = new HashMap<>();

    private static final Properties properties = new Properties();

    private static final String CGLIB_CLASS_SEPARATOR = "$$";

    public ApplicationContext() {
        /*
         * 实际从应用中文件中加载
         */
        initializeProperties();
        /*
         * 扫描并注册组件
         */
        registerBean();
        /*
         * 实例化bean
         */
        initBean();

        /*
         *执行依赖注入
         */
        injectDependencies();


    }

    private void injectDependencies() {
        beans.forEach((beanName, bean) -> {
            Class<?> targetClass = isCglibProxy(bean) ? bean.getClass().getSuperclass() : bean.getClass();
            for (Field field : targetClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    injectAutowiredField(beanName, bean, field);
                } else if (field.isAnnotationPresent(Value.class)) {
                    injectValueField(beanName, bean, field);
                }
            }

        });
    }

    public static boolean isCglibProxy(Object object) {
        return object.getClass().getName().contains(CGLIB_CLASS_SEPARATOR);
    }

    private void injectValueField(String beanName, Object bean, Field field) {
        String annotationValue = field.getAnnotation(Value.class).value();
        String property = properties.getProperty(annotationValue);
        field.setAccessible(true);
        try {
            if (field.getType() == String.class) {
                field.set(bean, property);
            } else if (field.getType() == int.class || field.getType() == Integer.class) {
                field.set(bean, Integer.parseInt(property));
            } else if (field.getType() == long.class || field.getType() == Long.class) {
                field.set(bean, Long.parseLong(property));
            } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                field.set(bean, Boolean.parseBoolean(property));
            }
        } catch (IllegalAccessException e) {
            log.info("Injecting dependency value field {} to class {} error", field.getName(), beanName);
            throw new RuntimeException(e);
        }
        log.info("Injecting dependency value field {} to class {} success", field.getName(), beanName);
    }

    private void injectAutowiredField(String beanName, Object bean, Field field) {
        Object dependency = beans.get(field.getName());

        if (dependency == null) {
            log.info("Injecting dependency field {} to class {} fail, dependency field {} is empty", field.getName(), beanName, field.getName());
        }
        try {

            field.setAccessible(true);
            field.set(bean, dependency);
        } catch (IllegalAccessException e) {
            log.info("Injecting dependency field {} to class {} error", field.getName(), beanName);
            throw new RuntimeException(e);
        }
        log.info("Injecting dependency field {} to class {} success", field.getName(), beanName);
    }


    private void initBean() {
        beanDefinitions.forEach((beanName, bean) -> {
            try {
                boolean checkAuth =false;
                for (Method method : bean.getDeclaredMethods()) {
                    if(method.isAnnotationPresent(Auth.class)){
                        checkAuth = true;
                        break;
                    }
                }

                if(checkAuth){
                    beans.put(beanName, ProxyFactory.createProxy(bean));
                }else {
                    beans.put(beanName, bean.getDeclaredConstructor().newInstance());
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void registerBean() {
        Reflections reflection = new Reflections();
        Set<Class<?>> controllers = reflection.getTypesAnnotatedWith(Controller.class);
        Set<Class<?>> services = reflection.getTypesAnnotatedWith(Service.class);
        Set<Class<?>> components = reflection.getTypesAnnotatedWith(Component.class);
        Set<Class<?>> repositories = reflection.getTypesAnnotatedWith(Repository.class);
        Set<Class<?>> beanSet = new HashSet<>(controllers);
        beanSet.addAll(services);

        beanSet.addAll(components);
        beanSet.addAll(repositories);

        reflection.getTypesAnnotatedWith(Auth.class);
        beanSet.forEach(bean -> {
            String beanName = resolveBeanName(bean);
            beanDefinitions.put(beanName, bean);
            typeToBeanName.put(bean, beanName);
            for (Class<?> anInterface : bean.getInterfaces()) {
                List<String> childNames = classToImplNames.get(anInterface);
                if (childNames == null) {
                    childNames = new ArrayList<>();
                }
                childNames.add(beanName);
                classToImplNames.put(anInterface, childNames);
            }

        });
    }


    private void initializeProperties() {
        properties.setProperty("database.url", "jdbc:mysql://192.168.200.121:3306/lany?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false");
        properties.setProperty("username", "root");
        properties.setProperty("password", " ychg@123");

    }


    private String resolveBeanName(Class<?> bean) {
        Controller controller = bean.getAnnotation(Controller.class);
        Service service = bean.getAnnotation(Service.class);
        Component component = bean.getAnnotation(Component.class);
        Repository repository = bean.getAnnotation(Repository.class);

        if (controller != null && !controller.value().isEmpty()) {
            return controller.value();
        }
        if (service != null && !service.value().isEmpty()) {
            return service.value();
        }
        if (component != null && !component.value().isEmpty()) {
            return component.value();
        }
        if (repository != null && !repository.value().isEmpty()) {
            return repository.value();
        }
        String className = bean.getSimpleName();
        if (bean.getInterfaces().length == 1) {
            className = bean.getInterfaces()[0].getSimpleName();
        }

        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }

    public static <T> T getBean(Class<T> beanClass) {

        String beanName = typeToBeanName.get(beanClass);
        if (beanName != null) {
            return beanClass.cast(beans.get(beanName));
        }
        //查找接口实现
        if(beanClass.isInterface()){
            List<String> childNames = classToImplNames.get(beanClass);
            if (childNames != null) {
                for (String childName : childNames) {//接口可能有多个实现类，暂时只处理一种
                    return beanClass.cast(beans.get(childName));
                }
            }
        }

        return null;
    }
}

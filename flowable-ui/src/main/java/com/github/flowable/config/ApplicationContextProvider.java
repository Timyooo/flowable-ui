package com.github.flowable.config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author yangjf
 * @version 0.1.0
 * @time 2018/10/9/009
 */
@Component
@Slf4j
public class ApplicationContextProvider
        implements ApplicationContextAware {
    /**
     * 上下文对象实例
     */
    private static ApplicationContext applicationContext;

    private static Configuration configuration;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextProvider.applicationContext = applicationContext;
    }


    public static void removeHandlerMappings(String beanName) {
        //判断是否是handlers
        Class beanType = ApplicationContextProvider.getType(beanName);
        if (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class) || AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping.class)) {
            //属于handler
            unregisterMapping(beanType);
        }
    }

    public static void unregisterMapping(Class handler) {
        RequestMappingHandlerMapping handlerMapping = getBean(RequestMappingHandlerMapping.class);
        final Class<?> userType = ClassUtils.getUserClass(handler);
        try {
            Method getMapping = RequestMappingHandlerMapping.class.getDeclaredMethod("getMappingForMethod", Method.class, Class.class);
            getMapping.setAccessible(true);

            Method[] methods = userType.getMethods();
            for (Method method : methods) {
                if (AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class)) {
                    Object mapping = getMapping.invoke(handlerMapping, method, userType);
                    handlerMapping.unregisterMapping((RequestMappingInfo) mapping);
                }
            }

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取applicationContext
     *
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 通过name获取 Bean.
     *
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    /**
     * 通过class获取Bean.
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        try {
            return getApplicationContext().getBean(clazz);
        } catch (Exception ex) {
            ((DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory()).clearMetadataCache();
            throw new RuntimeException("获取serviceImpl失败");

        }
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     *
     * @param name
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    public static void removeBean(String beanName) {
        BeanDefinitionRegistry beanDefReg = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        beanDefReg.getBeanDefinition(beanName);
        if (beanDefReg.isBeanNameInUse(beanName)) {
            beanDefReg.removeBeanDefinition(beanName);
        }
    }

    public static Class getType(String name) {
        return getApplicationContext().getType(name);
    }


    public static void addBean(String name, Class clazz) {
        BeanDefinitionRegistry beanDefReg = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        if (!beanDefReg.isBeanNameInUse(name)) {
            beanDefReg.registerBeanDefinition(name, beanDefinitionBuilder.getBeanDefinition());
        }
    }


    public static Configuration getConfiguration() {
        if (null == configuration) {
            SqlSessionFactory sessionFactory = getBean(SqlSessionFactory.class);
            configuration = sessionFactory.getConfiguration();
        }
        return configuration;
    }
}
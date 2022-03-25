package com.nowcoder.community;

		import com.nowcoder.community.dao.AlphaDao;
		import com.nowcoder.community.service.AlphaService;
		import org.junit.Test;
		import org.junit.runner.RunWith;
		import org.springframework.beans.BeansException;
		import org.springframework.beans.factory.annotation.Autowired;
		import org.springframework.beans.factory.annotation.Qualifier;
		import org.springframework.boot.test.context.SpringBootTest;
		import org.springframework.context.ApplicationContext;
		import org.springframework.context.ApplicationContextAware;
		import org.springframework.test.context.ContextConfiguration;
		import org.springframework.test.context.junit4.SpringRunner;

		import java.text.SimpleDateFormat;
		import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
//测试环境想和朱代码有一样的配置类
@ContextConfiguration(classes = CommunityApplication.class)
public class CommunityApplicationTests implements ApplicationContextAware {
	//怎么得到容器，实现 ApplicationContextAware接口
	//用来纪录容器

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	//测试spring容器
	@Test
	public void testApplicationContext() {
		System.out.println(applicationContext);

		//怎么用它去管理bean,，先去写一个bean
		//从容器中获取这个类型的bean
		//bean管理的好处：如果再加一个类，想把之前的bean替换掉，只需要加一个Primary注解就可以get到，
		//调用的地方完全不用变，因为调用的时候依赖的不是本身，而是接口
		AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class);
		System.out.println(alphaDao.select());

		//通过名字强制get到想要的bean,可以在Repository里加改名后的名字
		alphaDao = applicationContext.getBean("alphaHibernate", AlphaDao.class);
		System.out.println(alphaDao.select());
	}
//测试bean的销毁和初始化
	@Test
	public void testBeanManagement() {
		AlphaService alphaService = applicationContext.getBean(AlphaService.class);
		System.out.println(alphaService);

		//再实例化一次，发现只实例化一次，是单例，如果想不是单例，可以在在类前加@Scope(""prototype)
		alphaService = applicationContext.getBean(AlphaService.class);
		System.out.println(alphaService);
	}

	@Test
	public void testBeanConfig() {
		SimpleDateFormat simpleDateFormat =
				applicationContext.getBean(SimpleDateFormat.class);
		System.out.println(simpleDateFormat.format(new Date()));
	}
//上面是主动获取，其实使用的时候可以使用依赖注入，需要注解，可以直接用
	//这个注解也可以写到构造器上，通过构造方法注入
	//或者通过set方法注入
	@Autowired
	//想要alphadao注入的是alphaHibernate
	@Qualifier("alphaHibernate")
	private AlphaDao alphaDao;

	@Autowired
	private AlphaService alphaService;

	@Autowired
	private SimpleDateFormat simpleDateFormat;

	@Test
	public void testDI() {
		System.out.println(alphaDao);
		System.out.println(alphaService);
		System.out.println(simpleDateFormat);
	}

}

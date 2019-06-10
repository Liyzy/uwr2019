package core.generator;
import core.common.*;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DataSourceConfig.class)
@PowerMockIgnore("javax.management.*")
public class TestTemplateProcessor implements DataSourceType{
	//待测试类(SUT)的一个实例。
	private TemplateProcessor tp;
	//依赖类(DOC)的一个实例。
	private DataSourceConfig dsc;

	@Test
	public void testStaticVarExtract() throws Exception {

		//设置待测试类的状态（测试目标方法）
		tp.staticVarExtract("resource/newtemplatezzz.doc");
		//以下进行检查点设置
		DataSource ds = dsc.getConstDataSource();

		List<DataHolder> dhs = ds.getVars();
		DataHolder dh1 = ds.getDataHolder("sex");
		assertNotNull("变量sex解析为空", dh1);
		assertEquals("变量sex值获取错误","Female",dh1.getValue());

		DataHolder dh2 = ds.getDataHolder("readme");
		assertNotNull("变量readme解析为空", dh2);
		assertEquals("变量readme值获取错误","5",dh2.getValue());

		DataHolder dh3 = ds.getDataHolder("testexpr");
		assertNotNull("变量testexpr", dh3);
		assertEquals("变量testexpr的表达式解析错误","${num}+${readme}",dh3.getExpr());
		dh3.fillValue();
		assertEquals("变量testexpr","5.0",dh3.getValue());
		
		// 增添静态行为测试
		assertNotNull("静态方法测试不通过", DataSourceConfig.newInstance());
		
		
		// 增添其他未使用方法的测试
		DataSource dsByName = dsc.getDataSource("test");
		assertNotNull("根据名字获取的DataSource为空", dsByName);
		ArrayList<DataSource> dss = dsc.getDataSources();
		assertNotNull("不存在任何DataSource", dss);
		assertEquals("newFile", dsc.getFilename());

		//检测SUT的实际行为模式是否符合预期
		PowerMock.verifyAll();
	}

	@Before
	public void setUp() throws Exception {

		//以下采用Mock对象的方式，做测试前的准备。
		//与以上方法比较，好处是降低SUT（TemplateProcessor类）与DOC（DataSourceConfig类）之间的耦合性，解耦它们。
		//从而使得定位缺陷变得容易。
		//参照流程：
		//1. 使用EasyMock建立一个DataSourceConfig类的一个Mock对象实例；
		//2. 录制该实例的STUB模式和行为模式（针对的是非静态方法）；
		//3. 使用PowerMock建立DataSourceConfig类的静态Mock；
		//4. 录制该静态Mock的行为模式（针对的是静态方法）；
        	//------------------------------------------------
        	//以上流程请在这里实现：
        	//
        	dsc = EasyMock.createMock(DataSourceConfig.class);

		ConstDataSource ds = EasyMock.createMock(ConstDataSource.class);
		DataHolder dataHolderMock1 = EasyMock.createMock(DataHolder.class);
		DataHolder dataHolderMock2 = EasyMock.createMock(DataHolder.class);
		DataHolder dataHolderMock3 = EasyMock.createMock(DataHolder.class);

		EasyMock.expect(dataHolderMock1.getValue()).andReturn("Female");
		EasyMock.expect(dataHolderMock2.getValue()).andReturn("5");
		EasyMock.expect(dataHolderMock3.getExpr()).andReturn("${num}+${readme}");
		EasyMock.expect(dataHolderMock3.fillValue()).andReturn(null);
		EasyMock.expect(dataHolderMock3.getValue()).andReturn("5.0");

		List<DataHolder> dataHolderList = new ArrayList<DataHolder>();
		dataHolderList.add(dataHolderMock1);
		dataHolderList.add(dataHolderMock2);
		dataHolderList.add(dataHolderMock3);

		EasyMock.expect(dsc.getConstDataSource()).andReturn(ds);
		EasyMock.expect(ds.getVars()).andReturn(dataHolderList);
		EasyMock.expect(ds.getDataHolder(EasyMock.anyString())).andStubAnswer(new IAnswer<DataHolder>() {
			@Override
			public DataHolder answer() throws Throwable {
				String name = (String)EasyMock.getCurrentArguments()[0];
				if(name.equals("sex"))
					return dataHolderMock1;
				if(name.equals("readme"))
					return dataHolderMock2;
				if(name.equals("testexpr"))
					return dataHolderMock3;
				return null;
			}
		});
		
		PowerMock.mockStatic(DataSourceConfig.class);
		EasyMock.expect(DataSourceConfig.newInstance()).andReturn(dsc);

		// 原测试中未涉及的非静态方法测试
		EasyMock.expect(dsc.getDataSource("test")).andReturn(ds);
		ArrayList<DataSource> dataSourceList = new ArrayList<DataSource>();
		dataSourceList.add(ds);
		EasyMock.expect(dsc.getDataSources()).andReturn(dataSourceList);
		EasyMock.expect(dsc.getFilename()).andReturn("newFile");
        	//
        	//------------------------------------------------
		
		//5. 重放所有的行为。
		//PowerMock.replayAll(dsc);
		PowerMock.replayAll(dsc, ds, dataHolderMock1, dataHolderMock2, dataHolderMock3);
		
		//初始化一个待测试类（SUT）的实例
		tp = new TemplateProcessor();
	}
}

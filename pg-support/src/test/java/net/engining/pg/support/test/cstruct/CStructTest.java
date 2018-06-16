package net.engining.pg.support.test.cstruct;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import net.engining.pg.support.cstruct.CChar;
import net.engining.pg.support.cstruct.CStruct;

public class CStructTest {
	
	private enum Type {A001, B002};
	
	public static class T1
	{
		@CChar(value=5, order=200, leftPadding=true)
		public String str2 = "fdsa";
		
		@CChar(value=5, order=100)
		public String str1 = "asdf";
		
		@CChar(value=8, order=300, formatPattern="{0,Number,0}", zeroPadding=false)
		public Integer int1 = 1234;

		@CChar(value=8, order=400, zeroPadding=true)
		public Integer int2 = -5678;

		@CChar(value=8, order=500, zeroPadding=true)
		public Integer int3 = 1122;
		
		@CChar(value=10, order=600, precision = 2, zeroPadding = true)
		public BigDecimal amount = new BigDecimal("123.1");
		
		@CChar(value=10, order=700)
		public Type type = Type.B002;
		
		@CChar(value=8, order=800,datePattern="yyyyMMdd")
		public Date date;
	};
	
	@Test
	public void normalBuild() throws UnsupportedEncodingException, ParseException
	{
		CStruct<T1> cs = new CStruct<T1>(T1.class, "utf-8");
		T1 t = new T1();
		t.date = new SimpleDateFormat("yyyyMMdd").parse("20121212");
		ByteBuffer buffer = ByteBuffer.allocate(cs.getByteLength());
		cs.writeByteBuffer(t, buffer);
		assertThat(new String(buffer.array(), "utf-8"), equalTo(
				"asdf" +
				"  fdsa" +
				"1234    " +
				"-0005678" +
				"00001122" +
				"0000012310" +
				"B002      " +
				"20121212"));
	}
	
	@Test
	public void normalParse() throws UnsupportedEncodingException, ParseException
	{
		CStruct<T1> cs = new CStruct<T1>(T1.class, "utf-8");
		T1 t = cs.parseByteBuffer(ByteBuffer.wrap((
				"asdf" +
				"  fdsa" +
				"1234    " +
				"-0015678" +
				"00001122" +
				"0000456789" +
				"B002      " +
				"20121212"
				).getBytes("utf-8")));
		assertThat(t.int2, equalTo(-15678));
		assertThat(t.int3, equalTo(1122));
		assertThat(t.amount, equalTo(new BigDecimal("4567.89")));
		assertThat(t.date, equalTo(new SimpleDateFormat("yyyyMMdd").parse("20121212")));
	}
	
	public static class T2
	{
		@CChar(value=10, order = 0)
		public BigDecimal num1;
		
		@CChar(value=10, order = 1)
		public Integer num2;
	}

	@Test
	public void nullNumber() throws UnsupportedEncodingException
	{
		//使用空格来表达null值的数字
		CStruct<T2> cs = new CStruct<T2>(T2.class, "utf-8");
		T2 t2 = new T2();
		ByteBuffer buffer = ByteBuffer.allocate(cs.getByteLength());
		cs.writeByteBuffer(t2, buffer);
		assertThat(new String(buffer.array(), "utf-8"), equalTo("                    "));
	}
	
	@Test
	public void nullNumberParse()
	{
		CStruct<T2> cs = new CStruct<T2>(T2.class);
		T2 t2 = cs.parseByteBuffer(ByteBuffer.wrap(
			"                    ".getBytes()));
		assertThat(t2.num1, equalTo(null));
		assertThat(t2.num2, equalTo(null));
	}
	@Test
	public void trimNumberParse()
	{
		CStruct<T2> cs = new CStruct<T2>(T2.class);
		T2 t2 = cs.parseByteBuffer(ByteBuffer.wrap(
			"       123       123".getBytes()));
		assertThat(t2.num1, equalTo(BigDecimal.valueOf(123)));
		assertThat(t2.num2, equalTo(123));
	}
}

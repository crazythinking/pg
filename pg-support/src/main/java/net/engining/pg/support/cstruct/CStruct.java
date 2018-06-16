package net.engining.pg.support.cstruct;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 根据注释配置处理C风格的结构体.
 * TODO 在处理时的迭代需要优化以提高性能。
 * @author licj
 *
 * @param <T>
 */
public class CStruct<T> {
	
	private Logger logger = LoggerFactory.getLogger(getClass()); 
	
	private List<Field> fields = new ArrayList<Field>();
	
	private String charset;
	
	private Class<T> clazz;
	
	private int byteLength;
	
	/**
	 * 默认使用gbk编码
	 * @param clazz
	 */
	public CStruct(Class<T> clazz)
	{
		this(clazz, "gbk");
	}
	
	public CStruct(Class<T> clazz, String charset)
	{
		this.clazz = clazz;
		Collections.addAll(this.fields, clazz.getFields());	//缓存一下
		this.charset = charset;
		
		byteLength = calcByteLength();
		//排序字段
		Collections.sort(fields, new Comparator<Field>() {
			@Override
			public int compare(Field o1, Field o2) {
				
				int order1 = getOrder(o1);
				int order2 = getOrder(o2);

				//这段复制自 Integer.compareTo
				return (order1 < order2 ? -1 : (order1 == order2 ? 0 : 1));
			}

			private int getOrder(Field field)
			{
				CChar cc = field.getAnnotation(CChar.class);
				if (cc != null)
					return cc.order();
				CBinaryInt cbi = field.getAnnotation(CBinaryInt.class);
				if (cbi != null)
					return cbi.order();
				return 0;
			}
		});
		//TODO 校验设置
	}
	
	/**
	 * @return 计算整个结构体长度
	 */
	private int calcByteLength()
	{
		int sum = 0;
		for (Field field : fields)
		{
			if ((field.getModifiers() & Modifier.STATIC) > 0)
				continue;
			
			CChar annoChar = field.getAnnotation(CChar.class);
			CBinaryInt annoInt = field.getAnnotation(CBinaryInt.class);
			
			if (annoChar != null)
				sum += annoChar.value();
			else if (annoInt != null)
				sum += annoInt.length();
		}
		return sum;
	}

	/**
	 * 生成数据缓冲，并且额外增加指定字节数
	 * @param source
	 * @param additionalBytes
	 */
	public void writeByteBuffer(T source, ByteBuffer buffer)
	{
		assert source != null;
		
		try 
		{
			for (Field field : fields)
			{
				if ((field.getModifiers() & Modifier.STATIC) > 0)
					continue;
				Class<?> type = field.getType();
				
				Object value = field.get(source);
				
				CChar annoChar = field.getAnnotation(CChar.class);
				CBinaryInt annoInt = field.getAnnotation(CBinaryInt.class);
				
				if (annoChar != null)
				{
					byte[] out;

					//按字段类型分别处理。优先处理null以及有formatPattern的字段
					
					if (value == null)
						out = "".getBytes(charset);						//所有的null值均用全空格来表示
					else if (StringUtils.isNotBlank(annoChar.formatPattern()))	//优先考虑pattern，有pattern就使用pattern来格式化了
					{
						out = MessageFormat.format(annoChar.formatPattern(), value).getBytes(charset);
					}
					else if (type.equals(Date.class))		//日期类型
					{
						if (StringUtils.isNotBlank(annoChar.datePattern()))		//有日期模版
						{
							SimpleDateFormat sdf = new SimpleDateFormat(annoChar.datePattern());
							out = sdf.format((Date)value).getBytes(charset);
							//这里假定pattern的长度与value()一样，需要在构造函数里设置
						}
						else
						{
							out = value.toString().getBytes(charset);	//直接toString，几乎肯定不是要的格式
							logger.warn("使用toString格式化Date类型字段[{}/{}](是否漏加了datePattern属性？)", clazz.getCanonicalName(), field.getName());
						}
					}
					else if (type.equals(String.class))		//字符串
					{
						out = ((String)value).getBytes(charset);
					}
					else if (Number.class.isAssignableFrom(type))		//数字
					{
						//转成字符串
						
						//记录符号，只有负数才在最左边加上符号
						String sign = "";
						String tempValue;
						if (type.equals(BigDecimal.class))
						{
							//不输出小数点
							BigDecimal bd = (BigDecimal)value;
							bd = bd.setScale(annoChar.precision(), annoChar.rounding());
							if (bd.signum() == -1)
							{
								//负数
								sign = "-";
								bd = bd.abs();
							}
							tempValue = MessageFormat.format("{0,number,0}", bd.unscaledValue());	
						}
						else
						{
							//其它类型转成long型处理
							long num = ((Number)value).longValue();
							
							//用long型来判负
							if (num < 0)
							{
								sign = "-";
								num = -num;
							}
							tempValue = MessageFormat.format("{0,number,0}", num);
						}
						//补完0后再拼符号
						if (annoChar.zeroPadding())
						{
							//这里由于都是数字，所以长度与字节数一样，可以直接leftPad
							tempValue = StringUtils.leftPad(tempValue, annoChar.value() - sign.length(), "0");
						}
						out = (sign + tempValue).getBytes(charset);
					} else if (type.isArray() && type.getComponentType().equals(byte.class)) {
						out = (byte[]) value;
					} else if (Boolean.class.equals(type)) {
						out = (Boolean) value ? "1".getBytes() : "0".getBytes();
					}
					else
					{
						//其它类型就直接 toString了
						out = value.toString().getBytes(charset);
					}
					
					if (out.length > annoChar.value())
					{
						buffer.put(out, 0, annoChar.value());
					}
					else
					{
						if (annoChar.leftPadding())
						{
							//先输出空格
							for (int i = 0; i<annoChar.value() - out.length; i++)
								buffer.put((byte)' ');
							buffer.put(out);
						}
						else
						{
							//先输出内容
							buffer.put(out);
							for (int i = 0; i<annoChar.value() - out.length; i++)
								buffer.put((byte)' ');
						}
					}
				}
				else if (annoInt != null)
				{
					assert value instanceof Number;
					assert annoInt.length() >= 1 && annoInt.length() <= 8 : "二进制字段长度必须在1到8之间";
					
					long l = ((Number)value).longValue();
					byte bytes[] = new byte[annoInt.length()];
					for (int i=0; i<bytes.length; i++)
						bytes[i] = (byte)(l & 0xff);
					if (annoInt.bigEndian())
						for (int i=bytes.length - 1; i>=0; i--)
							buffer.put(bytes[i]);
					else
						buffer.put(bytes);
				}
				else
					assert false : field.getName() + " 必须指定字段类型注释";
			}
		}
		catch (Exception e) 
		{
			logger.error("结构体解析出错:" + clazz.getCanonicalName(), e);
			throw new IllegalArgumentException(e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public T parseByteBuffer(ByteBuffer buffer)
	{
		try 
		{
			T obj = clazz.newInstance();
			
			for (Field field : fields)
			{
				if ((field.getModifiers() & Modifier.STATIC) > 0)
					continue;
				Class<?> type = field.getType();
				
				CChar annoChar = field.getAnnotation(CChar.class);
				CBinaryInt annoInt = field.getAnnotation(CBinaryInt.class);
				
				if (annoChar != null)
				{
					int len = annoChar.value();
					byte bytes[] = new byte[len];
					buffer.get(bytes);
					
					String value = new String(bytes, charset);

					if (StringUtils.isBlank(value) && !annoChar.required() && !type.equals(String.class))
					{
						//可以忽略的字段，填null
						field.set(obj, null);
					}
					else if (type.equals(Date.class))
					{
						SimpleDateFormat sdf = new SimpleDateFormat(annoChar.datePattern());
						sdf.setLenient(true);
						field.set(obj, sdf.parse(value));
					}
					else if (type.equals(String.class))
					{
						if (annoChar.autoTrim())
							value = value.trim();
						//字符串直接赋值
						field.set(obj, value);
					}
					else if (type.equals(Integer.class) || type.equals(int.class))
					{
						field.set(obj, Integer.valueOf(value.trim()));
					}
					else if (type.equals(Long.class) || type.equals(long.class))
					{
						field.set(obj, Long.valueOf(value.trim()));
					}
					else if (type.equals(BigDecimal.class))
					{
						field.set(obj, BigDecimal.valueOf(Long.valueOf(value.trim()), annoChar.precision()));
					}
					else if (type.isEnum())
					{
						if (StringUtils.isNotBlank(value))
							field.set(obj, Enum.valueOf((Class<? extends Enum>)type, value.trim()));
					} else if (type.isArray() && type.equals(byte.class)) {
						field.set(obj, bytes);
					} else if (Boolean.class.equals(type)) {
						field.set(obj, Boolean.valueOf("1".equals(value) ? true : false));
					}						
					else
					{
						throw new IllegalArgumentException("不支持的字段类型:" + type);
					}
				}
				else if (annoInt != null)
				{
					int value = buffer.getInt();
					field.set(obj, value);
				}
				else
					assert false : field.getName() + " 必须指定字段类型注释";
			}
			
			return obj;
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * 获取当前结构的统计报表
	 * @return
	 */
	public String summaryReport()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("fld\tlen\tstart\n");
		int start = 0;
		for (Field field : fields)
		{
			CChar c = field.getAnnotation(CChar.class);
			if (c == null)
				continue;
			sb.append(field.getName());
			sb.append('\t');
			sb.append(c.value());
			sb.append('\t');
			sb.append(start);
			sb.append('\n');
			start += c.value();
		}
		return sb.toString();
	}

	public int getByteLength() {
		return byteLength;
	}
}

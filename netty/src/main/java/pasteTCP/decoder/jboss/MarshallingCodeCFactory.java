package pasteTCP.decoder.jboss;

import io.netty.handler.codec.marshalling.DefaultMarshallerProvider;
import io.netty.handler.codec.marshalling.DefaultUnmarshallerProvider;
import io.netty.handler.codec.marshalling.MarshallerProvider;
import io.netty.handler.codec.marshalling.MarshallingDecoder;
import io.netty.handler.codec.marshalling.MarshallingEncoder;
import io.netty.handler.codec.marshalling.UnmarshallerProvider;

import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;

/**
 * @author Lilinfeng
 * @date 2014年2月25日
 * @version 1.0
 */
public final class MarshallingCodeCFactory {

    /**
     * 创建Jboss Marshalling解码器MarshallingDecoder
     *
     * @return
     */
    public static MarshallingDecoder buildMarshallingDecoder() {
        //通过Marshalling静态方法获取marshallerFactory实例，参数serial表示创建的是java序列化工厂对象
        final MarshallerFactory marshallerFactory = Marshalling
                .getProvidedMarshallerFactory("serial");
        //创建了MarshallingConfiguration对象，版本号设置为5
        final MarshallingConfiguration configuration = new MarshallingConfiguration();
        configuration.setVersion(5);
        //根据工厂，配置创建UnmarshallerProvider
        UnmarshallerProvider provider = new DefaultUnmarshallerProvider(
                marshallerFactory, configuration);
        //最后根据构造函数创建Netty的MarshallingDecoder对象，其有两个参数，分别是UnmarshallerProvider和单个消息序列化的最大长度。
        MarshallingDecoder decoder = new MarshallingDecoder(provider, 1024);
        return decoder;
    }

    /**
     * 创建Jboss Marshalling编码器MarshallingEncoder
     *
     * @return
     */
    public static MarshallingEncoder buildMarshallingEncoder() {
        final MarshallerFactory marshallerFactory = Marshalling
                .getProvidedMarshallerFactory("serial");
        final MarshallingConfiguration configuration = new MarshallingConfiguration();
        configuration.setVersion(5);
        MarshallerProvider provider = new DefaultMarshallerProvider(
                marshallerFactory, configuration);
     //创建MarshallingEncoder实例，其用于将实现序列化接口的POJO对象序列化为二进制数组
        MarshallingEncoder encoder = new MarshallingEncoder(provider);
        return encoder;
    }
}
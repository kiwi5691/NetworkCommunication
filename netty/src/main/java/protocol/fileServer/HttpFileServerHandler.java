package protocol.fileServer;


import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.LOCATION;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;

/**
 * @author lilinfeng
 * @date 2014年2月14日
 * @version 1.0
 */
public class HttpFileServerHandler extends
        SimpleChannelInboundHandler<FullHttpRequest> {
    private final String url;

    public HttpFileServerHandler(String url) {
        this.url = url;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx,
                                FullHttpRequest request) throws Exception {
        //先对http请求消息的解码进行判断
        if (!request.getDecoderResult().isSuccess()) {
            //构造404错误
            sendError(ctx, BAD_REQUEST);
            return;
        }
        //不是GET请求
        if (request.getMethod() != GET) {
            //构造405错误
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }
        final String uri = request.getUri();
        final String path = sanitizeUri(uri);
        //如果构造的URI不合法，返回403
        if (path == null) {
            sendError(ctx, FORBIDDEN);
            return;
        }
        //使用新组装的URI路径构造FILE对象
        File file = new File(path);
        if (file.isHidden() || !file.exists()) {
            sendError(ctx, NOT_FOUND);
            return;
        }
        //如果文件是目录，则发送目录的连接给客户端浏览器
        if (file.isDirectory()) {
            if (uri.endsWith("/")) {
                sendListing(ctx, file);
            } else {
                sendRedirect(ctx, uri + '/');
            }
            return;
        }
        //如果用户点击超连接or直接打开，会对超连接的文件进行合法性判断，如果非合法，则403
        if (!file.isFile()) {
            sendError(ctx, FORBIDDEN);
            return;
        }
        RandomAccessFile randomAccessFile = null;
        try {
            //使用随机文件读写类以只读方式打开，如果失败则404
            randomAccessFile = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException fnfe) {
            sendError(ctx, NOT_FOUND);
            return;
        }
        //获取文件长度，构造成功的HTTP应答消息
        long fileLength = randomAccessFile.length();
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        //设置contentLength和ContentType
        setContentLength(response, fileLength);
        setContentTypeHeader(response, file);
        //判断是否KeepAlive
        if (isKeepAlive(request)) {
            //是则设置Connection为KeepAlive
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }
        //发送响应消息
        ctx.write(response);
        ChannelFuture sendFileFuture;
        //通过netty的ChunkedFile对象直接写入到发送缓冲区中。
        //最后为sendFileFuture增加ChannelProgressiveFutureListener，其为GenericFutureListener的子类
        sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile, 0,
                fileLength, 8192), ctx.newProgressivePromise());
        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future,
                                            long progress, long total) {
                if (total < 0) { // total unknown
                    System.err.println("Transfer progress: " + progress);
                } else {
                    System.err.println("Transfer progress: " + progress + " / "
                            + total);
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future)
                    throws Exception {
                System.out.println("Transfer complete.");
            }
        });
        //如果使用chunked编码，最后需要发送一个编码结束的空消息体，将lastHttpContent的EMPTY_LAST_CONTENT发送到缓冲区中
        //标识所有的消息体已经发送完成，同时调用flush方法将之前在发送缓冲区的消息刷新的SocketChannel中发送给对方
        ChannelFuture lastContentFuture = ctx
                .writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        //非KeepAlive的，最后一包消息发送完成后，服务端要主动关闭连接
        if (!isKeepAlive(request)) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

    private String sanitizeUri(String uri) {
        try {
            //如果解码成功后对URI进行合法判断
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new Error();
            }
        }
        //判断URI与允许访问的URI一致or其子目录（文件），则校验通过
        if (!uri.startsWith(url)) {
            return null;
        }
        if (!uri.startsWith("/")) {
            return null;
        }
        //将硬编码的文件路径分隔符替换为本地操作系统的文件路径分隔符
        uri = uri.replace('/', File.separatorChar);
        //对新URI做二次合法校验
        if (uri.contains(File.separator + '.')
                || uri.contains('.' + File.separator) || uri.startsWith(".")
                || uri.endsWith(".") || INSECURE_URI.matcher(uri).matches()) {
            return null;
        }
        //最后对文件进行拼接，使用当前运行程序所在的工程目录+URI构造绝对路径返回
        return System.getProperty("user.dir") + File.separator + uri;
    }

    private static final Pattern ALLOWED_FILE_NAME = Pattern
            .compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

    private static void sendListing(ChannelHandlerContext ctx, File dir) {
        //创建响应成功 HTTP响应消息
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        //随后设置类型为text/html; charset=UTF-8
        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        StringBuilder buf = new StringBuilder();
        String dirPath = dir.getPath();
        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>");
        buf.append(dirPath);
        buf.append(" 目录：");
        buf.append("</title></head><body>\r\n");
        buf.append("<h3>");
        buf.append(dirPath).append(" 目录：");
        buf.append("</h3>\r\n");
        buf.append("<ul>");
        buf.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
        //展现根目录下的所有文件和文件夹，同时使用超链接标识
        for (File f : dir.listFiles()) {
            if (f.isHidden() || !f.canRead()) {
                continue;
            }
            String name = f.getName();
            if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
                continue;
            }
            buf.append("<li>链接：<a href=\"");
            buf.append(name);
            buf.append("\">");
            buf.append(name);
            buf.append("</a></li>\r\n");
        }
        buf.append("</ul></body></html>\r\n");
        ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
        //将消息存放到HTTP应答消息中
        response.content().writeBytes(buffer);
        //释放缓冲区
        buffer.release();
        //调用writeAndFlush将响应消息发送到缓冲器并刷新到SocketChannel中
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND);
        response.headers().set(LOCATION, newUri);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendError(ChannelHandlerContext ctx,
                                  HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                status, Unpooled.copiedBuffer("Failure: " + status.toString()
                + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void setContentTypeHeader(HttpResponse response, File file) {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        response.headers().set(CONTENT_TYPE,
                mimeTypesMap.getContentType(file.getPath()));
    }
}
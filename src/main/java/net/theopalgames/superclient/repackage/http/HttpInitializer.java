package net.theopalgames.superclient.repackage.http;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLEngine;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.theopalgames.superclient.SuperClientCallback;

public class HttpInitializer extends ChannelInitializer<Channel>
{

    private final SuperClientCallback<String> callback;
    private final boolean ssl;
    private final String host;
    private final int port;
    
    public HttpInitializer(SuperClientCallback<String> callback, boolean ssl, String host, int port) {
    	this.callback = callback;
    	this.ssl = ssl;
    	this.host = host;
    	this.port = port;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception
    {
        ch.pipeline().addLast( "timeout", new ReadTimeoutHandler( HttpClient.TIMEOUT, TimeUnit.MILLISECONDS ) );
        if ( ssl )
        {
            SSLEngine engine = SslContext.newClientContext().newEngine( ch.alloc(), host, port );

            ch.pipeline().addLast( "ssl", new SslHandler( engine ) );
        }
        ch.pipeline().addLast( "http", new HttpClientCodec() );
        ch.pipeline().addLast( "handler", new HttpHandler( callback ) );
    }
}

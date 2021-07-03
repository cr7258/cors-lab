## 什么是跨域？
### 同源策略

跨域问题其实就是浏览器的同源策略所导致的。同源策略是一个重要的安全策略，它用于限制一个 origin 的文档或者它加载的脚本如何能与另一个源的资源进行交互。它能帮助阻隔恶意文档，减少可能被攻击的媒介。

当跨域时会收到以下错误：

![](https://img-blog.csdnimg.cn/img_convert/833235222c9ce28a710298b235a7c02e.png)

**跨域是浏览器还是服务器的限制？**
当一个跨域请求在浏览器端发送出去后，后端服务会收到的请求并且也会处理和响应，只不过**浏览器**在解析这个请求的响应之后，发现不满足浏览器的同源策略（协议、域名和端口号均相同），也没有包含正确的 CORS 响应头，就拦截了这个响应。

### 怎么样算是同源

那么如何才算是同源呢？先来看看 url 的组成部分：

http://www.example.com:80/path/to/myfile.html?key1=value1&key2=value2#SomewhereInTheDocument

![](https://img-blog.csdnimg.cn/img_convert/9791b6705bd4c13b44623903c563c376.png)

只有当 protocol（协议）、domain（域名）、port（端口）三者一致时才是同源，后面的请求路径、请求参数、锚点可以不一致。

接下来看几个例子：

![](https://img-blog.csdnimg.cn/img_convert/ebc1835d4a523079c8ea259eb5989aa3.png)


## 跨域示例

![](https://img-blog.csdnimg.cn/img_convert/d960651a4ded39d5868b758e10f343db.png)

**设置前端服务**

创建 index.html 使用 fetch 调用 http://127.0.0.1:3011/api/data

```html
<html>
  <body>
    <script>
      fetch('http://127.0.0.1:3011/api/data');
    </script>
  </body>
</html>
```
创建 client.js 用来加载上面 index.html。监听 3010 端口。
```js
const http = require('http');
const fs = require('fs');
const PORT = 3010;
http.createServer((req, res) => {
  fs.createReadStream('index.html').pipe(res);
}).listen(PORT);
console.log('Server listening on port ', PORT);
```

启动前端服务：

```sh
node client.js
```

**设置后端服务**

创建 server.js 开启一个服务，提供一个访问的接口 /api/data，监听 3011 端口。


```js
const http = require('http');
const PORT = 3011;

http.createServer((req, res) => {
  const url = req.url;
  console.log('request url: ', url);
  if (url === '/api/data') {
    return res.end('console.log("hello world!");');
  }
}).listen(PORT);

console.log('Server listening on port ', PORT);
```

启动后端服务：

```sh
node server.js
```

在浏览器上访问前端服务 http://localhost:3010/ ，在请求头里可以看到有 Origin 字段，显示了我们当前的请求源信息。另外还有三个 Sec-Fetch-* 开头的字段，这是一个新的草案 Fetch Metadata Request Headers。

`Sec-Fetch-Mode: cors` 表示的是这是个跨域请求。

![](https://img-blog.csdnimg.cn/img_convert/80c546b5988637d9fcb1fb390e409540.png)

看下浏览器 Console 下的日志信息，根据提示得知原因是从 “http://127.0.0.1:3010” 访问 “http://127.0.0.1:3011/api/data” 被 CORS 策略阻止了，因为没有 “Access-Control-Allow-Origin” 标头。

![](https://img-blog.csdnimg.cn/img_convert/ea8ce87efc7d17683e7036a4cfd9c028.png)

再看下后端服务的输出，可以看到后端服务收到并且正常响应了请求，只不过浏览器在解析这个请求的响应之后，发现不满足浏览器的同源策略（协议、域名和端口号均相同），也没有包含正确的 CORS 响应头，就拦截了这个响应。

```sh
Server listening on port  3011
request url:  /api/data
```

本节代码示例：

```sh
https://github.com/cr7258/cors-lab/tree/master/cross-origin
```

## CORS（跨域资源共享）

跨源资源共享 (CORS，Cross-origin resource sharing）是一种基于 HTTP  头的机制，该机制通过允许服务器标示除了它自己以外的其它 origin（域，协议和端口），这样浏览器可以访问加载这些资源。

### 预检请求

预检请求是在发送实际的请求之前，前端服务会先发送一个 OPTIONS 方法的请求向服务器确认，如果通过之后，浏览器才会发起真正的请求，这样可以避免跨域请求对服务端的数据造成影响。

看到这里你可能有疑问为什么上面的示例没有预检请求？因为 CORS 将请求分为了两类：简单请求和非简单请求。我们上面的情况属于简单请求，所以也就没有了预检请求。

让我们继续在看下简单请求和非简单请求是如何定义的。

### 简单请求

不会触发 CORS 预检请求。这样的请求为“简单请求”。若请求满足所有下述条件，则该请求可视为“简单请求”：

* 情况一: 使用以下方法(意思就是以下请求意外的都是非简单请求)
    * GET
    * HEAD
    * POST
* 情况二: 人为设置以下集合外的请求头
    * Accept
    * Accept-Language
    * Content-Language
    * Content-Type（需要注意额外的限制）
    * DPR
    * Downlink
    * Save-Data
    * Viewport-Width
    * Width
* 情况三：Content-Type的值仅限于下列三者之一：(**例如 application/json 为非简单请求**)
    * text/plain
    * multipart/form-data
    * application/x-www-form-urlencoded
* 情况四:
    * 请求中的任意 XMLHttpRequestUpload 对象均没有注册任何事件监听器；XMLHttpRequestUpload 对象可以使用 XMLHttpRequest.upload 属性访问。
* 情况五:
    * 请求中没有使用 ReadableStream 对象。

### 非简单请求

除了简单请求以外的都是非简单请求。

### 预检请求示例

**设置前端服务**

为 index.html 里的 fetch 方法增加一些设置，设置请求的方法为 PUT，请求头增加一个自定义字段 Test-Cors。

```html
<script>
  fetch('http://127.0.0.1:3011/api/data', {
    method: 'PUT',
    headers: {
      'Content-Type': 'text/plain',
      'Test-Cors': 'abc'
    }
  });
</script>
```

上述代码在浏览器执行时会发现是一个非简单请求，就会先执行一个预检请求，Request Headers 会有如下信息：

```sh
OPTIONS /api/data HTTP/1.1
Host: 127.0.0.1:3011
Access-Control-Request-Method: PUT
Access-Control-Request-Headers: content-type,test-cors
Origin: http://127.0.0.1:3010
Sec-Fetch-Mode: cors
```

可以看到有一个 OPTIONS 是预检请求使用的方法，该方法是在 HTTP/1.1 协议中所定义的，还有一个重要的字段 Origin 表示请求来自哪个源，后端服务则可以根据这个字段判断是否是合法的请求源。

Access-Control-Request-Method 告诉服务器，实际请求将使用 PUT 方法。
Access-Control-Request-Headers 告诉服务器，实际请求将使用两个头部字段 Content-Type，Test-Cors。这里如果 Content-Type 指定的为简单请求中的几个值，Access-Control-Request-Headers 在告诉服务器时，实际请求将只有 Test-Cors 这一个头部字段。

**设置后端服务**

上面讲解了前端服务的设置，同样的要使请求能够正常响应，还需后端服务的支持。
修改我们的 server.js 重点是设置 Response Headers 代码如下所示：

```js
const http = require('http');
const PORT = 3011;

http.createServer((req, res) => {
  const { url, method } = req;
  console.log('request url:', url, ', request method:', method);
  res.writeHead(200, {
    'Access-Control-Allow-Origin': 'http://127.0.0.1:3010',
    'Access-Control-Allow-Headers': 'Test-CORS, Content-Type',
    'Access-Control-Allow-Methods': 'PUT,DELETE',
    'Access-Control-Max-Age': 1728000
  });
  if (method === 'OPTIONS') {
    return res.end();
  }
  if (method === 'PUT' && url === '/api/data') {
    return res.end('ok!');
  }
  return res.end();
}).listen(PORT);

console.log('Server listening on port ', PORT);
```

首先预检请求时，浏览器给了服务器几个重要的信息 Origin、Method 为 PUT、Headers 为 Content-Type，Test-Cors 。后端服务在收到之后，也要做些设置，给予回应。

Access-Control-Allow-Origin 表示 “http://127.0.0.1:3010” 这个请求源是可以访问的，该字段也可以设置为 “*” 表示允许任意跨源请求。

Access-Control-Allow-Methods 表示服务器允许前端服务使用 PUT、DELETE 方法发起请求，可以一次设置多个，表示服务器所支持的所有跨域方法，而不单是当前请求那个方法，这样好处是为了避免多次预检请求。

Access-Control-Allow-Headers 表示服务器允许请求中携带 Test-CORS、Content-Type 字段，也可以设置多个。

Access-Control-Max-Age 表示该响应的有效期，单位为秒。在有效时间内，浏览器无须为同一请求再次发起预检请求。还有一点需要注意，该值要小于浏览器自身维护的最大有效时间，否则是无效的。

浏览器访问 http://127.0.0.1:3010 ，第一次先发出了 OPTIONS 请求，并且在请求头设置了本次请求的方法和 Headers 信息，后端服务在 Response 也做了回应，在 OPTIONS 成功之后，浏览器紧跟着才发起了我们本次需要的真实请求，如图右侧所示 Resquest Method 为 PUT。

![](https://img-blog.csdnimg.cn/img_convert/8f996f77cef36a5620524611124ef4bd.png)

注意这里访问 127.0.0.1:3010 和 localhost:3010 是不一样的，因为我们在后端服务设置的是 `'Access-Control-Allow-Origin': 'http://127.0.0.1:3010'`。如果浏览器访问 http://localhost:3010 ，由于不满足服务端设置允许的跨域源地址，服务端的响应将会被浏览器拦截。

本节代码示例：

```sh
https://github.com/cr7258/cors-lab/tree/master/options
```

## CORS 与认证

对于跨域的 XMLHttpRequest 或 Fetch 请求，浏览器是不会发送身份凭证信息的。例如我们要在跨域请求中发送 Cookie 信息，就要做些设置：

为了能看到效果，我先自定义了一个 cookie 信息 id=NodejsRoadmap。

重点是设置认证字段，本文中 fetch 示例设置 credentials: "include" 如果是 XMLHttpRequest 则设置 withCredentials:"include"


```html
<body>
  <script>
    //设置 Cookie 信息
    document.cookie = `id=NodejsRoadmap`;
    fetch('http://127.0.0.1:3011/api/data', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Test-Cors': 'abc',
      },
      credentials: "include"
    });
  </script>
</body>
```

经过以上设置，浏览器发送实际请求时会向服务器发送 Cookies，同时服务器也需要在响应中设置 Access-Control-Allow-Credentials 响应头。


```js
const http = require('http');
const PORT = 3011;

http.createServer((req, res) => {
  const { url, method } = req;
  console.log('request url:', url, ', request method:', method);
  res.writeHead(200, {
    'Access-Control-Allow-Origin': 'http://127.0.0.1:3010',
    'Access-Control-Allow-Headers': 'Test-Cors, Content-Type',
    'Access-Control-Allow-Methods': 'PUT,DELETE',
    'Access-Control-Max-Age': 1728000,
    //允许发送认证信息
    'Access-Control-Allow-Credentials': true
  });
  if (method === 'OPTIONS') {
    return res.end();
  }
  if (method === 'PUT' && url === '/api/data') {
    return res.end('ok!');
  }
  return res.end();
}).listen(PORT);

console.log('Server listening on port ', PORT);
```
浏览器访问 http://127.0.0.1:3010，第一次发送 OPTIONS 请求，后端服务返回 `Access-Control-Allow-Credentials: true` 允许前端服务携带认证信息。

![](https://img-blog.csdnimg.cn/img_convert/a6063a5f481634369d2247a76e99401d.png)

第二次发送 PUT 请求，在 Cookie 中携带 id=NodejsRoadmap。

![](https://img-blog.csdnimg.cn/img_convert/7a92e0b1e39165171020443fd2953cfe.png)

本节代码示例：

```sh
https://github.com/cr7258/cors-lab/tree/master/cookie
```

## 解决跨域

解决跨域有以下方式：
* 在前端服务解决跨域。
* 在后端服务解决跨域。
* 在 Nginx 等反向代理解决跨域。
* Websocket。
* 浏览器允许跨域。

### 前端服务解决跨域
#### JSONP
JSONP 主要就是利用了 script 标签没有跨域限制的这个特性来完成的。浏览器是允许像 link、img、script 标签在路径上加载一些内容进行请求，是允许跨域的，JSONP 的实现原理就是在 script 标签里面加载了一个链接，去访问服务器的某个请求。


```js
//index.html
<body>
    <script src="http://127.0.0.1:3011/api/data"></script>
</body>
```

JSONP 有一个限制就是只支持 GET 请求。

本节代码示例：

```sh
https://github.com/cr7258/cors-lab/tree/master/front/jsonp
```

### 后端解决跨域
#### Node.js 项目
##### 使用 CORS 模块

如果后端是使用 Node.js 编写的，可以使用 cors 模块，github 地址：github.com/expressjs/cors 。在前面的示例中，一直使用的 Node.js 原生模块来编写我们的示例，在引入 cors 模块后，可以按照如下方式改写：

```js
// server.js
const http = require('http');
const PORT = 3011;
const corsMiddleware = require('cors')({
  origin: 'http://127.0.0.1:3010',
  methods: 'PUT,DELETE',
  allowedHeaders: 'Test-Cors, Content-Type',
  maxAge: 1728000,
  credentials: true,
});

http.createServer((req, res) => {
  const { url, method } = req;
  console.log('request url:', url, ', request method:', method);
  const nextFn = () => {
    if (method === 'PUT' && url === '/api/data') {
      return res.end('ok!');
    }
    return res.end();
  }
  corsMiddleware(req, res, nextFn);
}).listen(PORT);
console.log('Server listening on port ', PORT);
```

cors 在预检请求之后或在预检请求里并选项中设置了 preflightContinue 属性之后才会执行 nextFn 这个函数，如果预检失败就不会执行 nextFn 函数。

本节代码示例：

```sh
https://github.com/cr7258/cors-lab/tree/master/backend/cors
```

#### SpringBoot 项目

现在后端项目大部分使用 SpringBoot 编写的，接下来介绍 3 种在 SpringBoot 设置跨域的方式。

##### 方式一：使用 CorsFilter 进行全局跨域配置

```java
package com.chengzw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/***
 * @author chengzw
 * @description 后端解决跨域方式一
 * @since 2021/7/1
 */
@Configuration
public class CorsConfig {
    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //  你需要跨域的地址  注意这里的 127.0.0.1 != localhost
        // * 表示对所有的地址都可以访问
        corsConfiguration.addAllowedOrigin("*");
        //  跨域的请求头
        corsConfiguration.addAllowedHeader("*");
        //  跨域的请求方法
        corsConfiguration.addAllowedMethod("*");
        //加上了这一句，大致意思是可以携带 cookie
        //最终的结果是可以 在跨域请求的时候获取同一个 session
        corsConfiguration.setAllowCredentials(true);
        return corsConfiguration;
    }
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //配置可以访问的路径
        source.registerCorsConfiguration("/**", buildConfig()); // 4
        return new CorsFilter(source);
    }
}
```

##### 方式二：重写 WebMvcConfigurer 的 addCorsMappings方法（全局跨域配置）

```java
package com.chengzw.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author chengzw
 * @description 后端解决跨域方式二
 * @since 2021/7/3
 */
@Configuration
public class WebMvcConfg implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //设置允许跨域的路径
        registry.addMapping("/**")
                //设置允许跨域请求的域名
                //当**Credentials为true时，**Origin不能为星号，需为具体的ip地址【如果接口不带cookie,ip无需设成具体ip】
                .allowedOrigins("http://127.0.0.1:3010")
                //是否允许证书 不再默认开启
                .allowCredentials(true)
                //设置允许的方法
                .allowedMethods("*")
                //跨域允许时间
                .maxAge(3600);
    }
}
```
##### 方式三：使用CrossOrigin注解（局部跨域配置）

@CrossOrigin 注解是 Springboot 自带的，使用比较简单，只需要在支持的跨域的接口上加上这个注解就可以了。

将 @CrossOrigin 注解加在 Controller 层的方法上，该方法定义的 RequestMapping 端点将支持跨域访问。

将 @CrossOrigin 注解加在 Controller 层的类定义处，整个类所有的方法对应的 RequestMapping 端点都将支持跨域访问。


```java
package com.chengzw.controller;

import com.chengzw.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chengzw
 * @description
 * @since 2021/7/3
 */

@RestController
public class HelloController {

    @Autowired
    HelloService helloService;

    // 后端解决跨域问题方式三，使用该注解就注释 CrosConfig 和 WebMvcConfig，@CrossOrigin(origins = "*",allowCredentials="true",allowedHeaders = "*",methods = {})
    @CrossOrigin
    @RequestMapping("/api/data")
    public String hello(){
        return helloService.hello();
    }
}
```

本节代码示例：

```sh
https://github.com/cr7258/cors-lab/tree/master/backend/springboot
```

### Nginx 解决跨域

#### 方式一：添加跨域 HTTP 头部

![](https://img-blog.csdnimg.cn/img_convert/ece1936e48307061fe97b53dfd8a4a9a.png)

原本浏览器是访问 localhost:3011/api/data 请求后端服务的接口，现在让 Nginx 监听 3011 端口，把请求转发到后端服务新的端口 30011 上。Nginx 接收到后端服务的响应后，添加相关的 CORS 头部返回给返回给浏览器。

**Nginx 配置文件**

```nginx
events {}
http {
    server {
      listen          3011;
      server_name     localhost;
      location / {
        if ($request_method = 'OPTIONS') {
          add_header 'Access-Control-Allow-Origin' 'http://127.0.0.1:3010';
          add_header 'Access-Control-Allow-Methods' 'PUT,DELETE';
          add_header 'Access-Control-Allow-Headers' 'Test-CORS, Content-Type';
          add_header 'Access-Control-Max-Age' 1728000;
          add_header 'Access-Control-Allow-Credentials' 'true';
          add_header 'Content-Length' 0;
          return 204;
        }
    
        #Access-Control-Allow-Origin是必须的，其他可选
        add_header 'Access-Control-Allow-Origin' 'http://127.0.0.1:3010';  
        add_header 'Access-Control-Allow-Credentials' 'true';
    
        proxy_pass http://127.0.0.1:30011;
        proxy_set_header Host $host;
      }
    }
}
```

proxy_set_header 和 add_header 的区别？

proxy_set_header 是 Nginx 设置请求头信息给上游服务器，add_header 是 Nginx 设置响应头信息给浏览器。

启动 Nginx：

```sh
/usr/local/nginx/sbin/nginx -c ~/Code/github/cors-lab/nginx/nginx-add-header/nginx.conf
```

本节代码示例：

```sh
https://github.com/cr7258/cors-lab/tree/master/nginx/nginx-add-header
```

#### 方式二：让前端服务和后端服务接口同域

![](https://img-blog.csdnimg.cn/img_convert/c88b41d82db9cb3a55005991affeb571.png)

Nginx 对浏览器暴露统一的端口号 80，根据不同的请求请求路径区分前端服务和后端服务。
这样可以保证浏览器不管访问前端服务还是后端服务，看到的都是 http://localhost。

**Nginx 配置文件**

```nginx
server {
    listen       80;
    server_name  localhost;

    #前端服务
    location / {
        proxy_pass http://localhost:3010;
    }
    #后端服务接口地址
    location /api/data {
        proxy_pass http://localhost:3011;
    }
}
```

**修改前端服务**
```html
<html>
  <body>
    <script>
      //修改请求后端服务的地址，端口号改成 Nginx 监听的 80 端口
      fetch('http://127.0.0.1/api/data');
    </script>
  </body>
</html>
```

启动 Nginx：

```sh
/usr/local/nginx/sbin/nginx -c ~/Code/github/cors-lab/nginx/nginx-same-site/nginx.conf
```

本节代码示例：


```sh
https://github.com/cr7258/cors-lab/tree/master/nginx/nginx-same-site
```

### Websocket

WebSocket 规范定义了一种  API，可在网络浏览器和服务器之间建立“套接字”连接。简单来说浏览器和后端服务之间建立长连接，而且双方都可以随时开始发送数据。这种方式的本质是没有使用 HTTP 的响应头, 因此也没有跨域的限制。[Websock 可以参考这篇文章](https://mp.weixin.qq.com/s?__biz=MzkxOTIwMDgxMg==&mid=2247484123&idx=1&sn=37cb8ad655a053cd7a65b1493c3eeeaa&chksm=c1a4f798f6d37e8e4aa4525eefce9bf4caa02f1f040023be5c4a86d6935a6f3c6e5f1883acef&token=1041841332&lang=zh_CN#rd)。

**设置前端服务**
```html
<script>
  const socket = new WebSocket("ws://localhost:3011");
  socket.onopen = function() {
    socket.send("Hello Server");
  };
  socket.onmessage = function(e) {
    console.log(e.data);
  };
</script>
```

```js
const http = require('http');
const fs = require('fs');
const PORT = 3010;
http.createServer((req, res) => {
  fs.createReadStream('index.html').pipe(res);
  }).listen(PORT);
console.log('Server listening on port ', PORT);
```

**设置后端服务**

```js
const PORT = 3011;
const WebSocket = require("ws");
const server = new WebSocket.Server({ port: PORT });
server.on("connection", function(socket) {
  socket.on("message", function(data) {
    socket.send("Hello Client");
  });
});
console.log('Server listening on port ', PORT);
```

浏览器访问 http://127.0.0.1:3010，Websocket 是基于 HTTP 的，第一次浏览器发送 HTTP 请求，后端服务返回 101 Switching Procotols 升级 HTTP 为 Websocket。

![](https://img-blog.csdnimg.cn/img_convert/30fd955cd2079e3256673ce5b2125dd2.png)

![](https://img-blog.csdnimg.cn/img_convert/ecfedd2f9971f62649937386e8c39a28.png)

本节代码示例：

```sh
https://github.com/cr7258/cors-lab/tree/master/websocket
```

### 浏览器允许跨域

其实跨域问题是浏览器策略，源头是他，那么能否能关闭这个功能呢？
答案是肯定的。


**Windows**
找到安装 Chrome 的目录，--user-data-dir 这个目录可以自定义。
```sh

.\Google\Chrome\Application\chrome.exe --disable-web-security --user-data-dir=xxxx
```
**Mac**

在控制台敲入下面的命令前，先关闭已经打开的所有 Chrome 浏览器窗口。

```sh
#创建临时目录
mkdir /tmp/google
#设置跨域并打开浏览器
open -a "/Applications/Google Chrome.app" --args --disable-web-security  --user-data-dir=/tmp/google
```

![](https://img-blog.csdnimg.cn/img_convert/97e605efebabcca3fb9ce28a7220505f.png)

重启浏览器，如果还需要可跨域的话，依然需要通过这个命令行的形式打开，否则将通过普通的方式打开。

## 参考链接
* https://developer.mozilla.org/zh-CN/docs/Web/HTTP/CORS
* https://www.jianshu.com/p/a638fb1c8b34
* https://segmentfault.com/a/1190000021711445
* https://segmentfault.com/a/1190000022398875
* https://juejin.cn/post/6844903991558537223#heading-8

## 欢迎关注
![](https://img-blog.csdnimg.cn/img_convert/e49e60ce7931530a9e9bb4f1ee942941.png)

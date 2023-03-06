# OALEFT Snow

> 一个用于在Mybatis中打印执行SQL组件

## 介绍

## Requirements

- Java 17
- Maven 3.8.5

### GPG

[GPG Requirements](https://central.sonatype.org/publish/requirements/gpg/)

#### 1. 查看版本

```shell
gpg --version
```

#### 2. 生成 Key

```shell
gpg --gen-key
```

#### 3. 列出

```shell
gpg --list-keys
gpg --list-signatures
gpg --list-signatures --keyid-format 0xshort
```

#### 4. 生成和验证

```shell
gpg -ab myfile.java
gpg --verify myfile.java.asc 
```

#### 5. 发布公钥

```shell
# publish key
gpg --keyserver keyserver.ubuntu.com --send-keys CA925CD6C9E8D064FF05B4728190C4130ABA0F98
gpg --keyserver keys.openpgp.org --send-keys CA925CD6C9E8D064FF05B4728190C4130ABA0F98
# use key
gpg --keyserver keyserver.ubuntu.com --recv-keys CA925CD6C9E8D064FF05B4728190C4130ABA0F98
```

### Usage

```xml

<dependency>
    <groupId>com.oaleft.snow</groupId>
    <artifactId>snow-core</artifactId>
    <version>1.0.1</version>
</dependency>
```

使用 SpringBoot Starter

```xml

<dependency>
    <groupId>com.oaleft.snow</groupId>
    <artifactId>snow-spring-boot-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```

### 国内镜像库问题

> 如果使用

阿里镜像同步不及时，会出现导入报错的情况
如果使用了阿里的仓库代理，请在项目中配置以下仓库地址：

```xml

<repositories>
    <repository>
        <id>sonatype</id>
        <url>https://s01.oss.sonatype.org/content/groups/public/</url>
    </repository>
</repositories>
```

同时，在 settings.xml 的 `<mirror></mirror>` 中排除（阿里官方不代理 sonatype 库）

```xml

<mirrors>
    <mirror>
        <id>aliyun</id>
        <mirrorOf>*,!sonatype</mirrorOf>
        <name>Human Readable Name for this Mirror.</name>
        <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
</mirrors>
```

- https://repo1.maven.org/maven2/com/oaleft/
- https://developer.aliyun.com/mvn/guide
- https://maven.apache.org/repository/guide-central-repository-upload.html
- https://maven.apache.org/repositories/index.html
- https://github.com/qoomon/maven-git-versioning-extension/blob/master/README.md
- 
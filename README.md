# qiniu-sync-nas
[![experimental](http://badges.github.io/stability-badges/dist/experimental.svg)](http://github.com/badges/stability-badges)
[![MIT Licence](https://badges.frapsoft.com/os/mit/mit.svg?v=103)](https://opensource.org/licenses/mit-license.php)

#### 介绍
使用 Springboot2 结合 qiniu-sdk 同步七牛 OSS 文件到 NAS 本地。

#### 软件架构
Springboot2 + Gradle

#### 特性
- [X] 支持公开存储空间
- [X] 支持七牛图片瘦身(忽略文件校验)
- [X] 判断 OSS 文件是否更新
- [X] 同步策略（简单定时任务）
- [ ] 多线程下载
- [X] 支持私有存储空间
- [X] 支持空间开启原图保护和cdn图片瘦身
- [ ] 支持多存储空间(目前仅支持单一存储空间)
- [ ] ~~管理界面~~
- [ ] ~~支持Docker~~ 

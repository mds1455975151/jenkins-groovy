//发布job初始化

def Folder = 'Prod-Plugins'

def COMPILE_JOB_NAME = Folder+'/'+'Prod-Plugins-MsHospital-Compile' //编译job名称，不可以使用中文
def DEPLOY_JOB_NAME1 = Folder+'/'+'Prod-Plugins-MsHospital-Deploy1' //发布job名称，不可以使用中文
def DEPLOY_JOB_NAME2 = Folder+'/'+'Prod-Plugins-MsHospital-Deploy2' //发布job名称，不可以使用中文
def DEPLOY_JOB_NAME3 = Folder+'/'+'Prod-Plugins-MsHospital-Deploy3' //发布job名称，不可以使用中文
def PIPELINE_JOB_NAME = Folder+'/'+'Prod-Plugins-MsHospital-Pipeline'

def GITURL = 'http:// <git>/scm/qy-components/ms-hospital.git' //git仓库地址
def Git_AUTH = '6b5de867-3f96-4162-9384-4c81e0e7063c' //git仓库认证账号
def BRANCH = '*/release'  //发布分支

def OSS_BUCKET = 'deployment-package'  //oss存储bucket
def ENV = 'release'  //配置文件环境
def GROUP = 'plugins'  //所属组
def PRODUCTION = 'ms-hospital'   //产品名称，该参数表示oss存储路径和部署服务器路径中微服务名
def WAR_NAME = 'MS-Hospital-0.0.1.war'  //WAR包名称


def APP_NAME = 'ms-hospital'  //微服务名称,一般固定在代码里，若不修改则为空
def APP_OPTS = "--spring.application.name=$APP_NAME"   //此参数用于强制注入微服务名称，一般会固定在代码中，若不修改该参数为空

def ECS_IP1 = ' '
def ECS_IP2 = ' '
def ECS_IP3 = ' '
def Xms = '2048M'
def Xmx = '2048M'
def Threads = '2'
def JAVA_OPTS = "-Xms${Xms} -Xmx${Xmx} -XX:ParallelGCThreads=${Threads}"//java启动参数，若有需要请自行添加
def SHELL_NAME = 'Prod-MicroService'    //选择jumpserver发布脚本，按环境选择

//以下参数无特殊情况请勿修改
def DATE = '`date "+%Y-%m-%d"`'    //该参数为固定参数
def TIME = '`date "+%H:%M:%S "`'   //该参数为固定参数
def EUREKA_OPTS = '--eureka.instance.metadataMap.OldVersion=$OLD_VERSION --eureka.instance.metadataMap.LastVersion=$TAG --eureka.instance.metadataMap.WarName=$WAR_NAME' + " --eureka.instance.metadataMap.Date=$DATE --eureka.instance.metadataMap.Time=$TIME --eureka.instance.metadataMap.JavaOptXms=$Xms --eureka.instance.metadataMap.JavaOptXmx=$Xmx"


mavenJob("${COMPILE_JOB_NAME}") {
    logRotator(3, 3)       //参数为job保留时间，队列最大job构建数
    parameters {
      stringParam ('TAG',"")
      stringParam ('BRANCH',"$BRANCH")
      stringParam ('WAR_NAME',"$WAR_NAME")
      textParam ('Description',"")

    }

    label('node3')   //构建job节点选择
    goals('clean install -Dmaven.test.skip=true')
    rootPOM('pom.xml')
    mavenInstallation('maven-3.2.5')

    scm {
        git {
            remote {
              url("$GITURL")
              branch('$BRANCH')
              credentials("$Git_AUTH")
            }
        extensions{
              preBuildMerge {
                options{
                  mergeRemote('origin')
                  mergeTarget('master')
                  mergeStrategy('default')
                  fastForwardMode('FF')
                }
              }
            }
        }
    }

    publishers {
        gitPublisher{
          tagsToPush {
            tagToPush {
              targetRepoName('origin')
              tagName('$TAG')
              tagMessage('$Description')
              forcePush(false)
              createTag(true)
              updateTag(true)
            }
          }
          pushOnlyIfSuccess(true)
          pushMerge(true)
        		}
            }
    publishers {
        aliyunOSSPublisher {
          bucketName("$OSS_BUCKET")
          filesPath('target/$WAR_NAME')
          objectPrefix("$ENV/$GROUP/$PRODUCTION/"+'$TAG')
        }
    }
}

freeStyleJob("$DEPLOY_JOB_NAME1") {
    logRotator(3, 3)
    parameters {
        password {
              name('JPS_PWD')
              defaultValue('')
              description('')
        }
        stringParam ('TAG',"")
        stringParam ('OLD_VERSION',"")
        stringParam ('WAR_NAME',"$WAR_NAME")
        textParam ('JPS_PARAMETER',"{'PRODUCTION':'$PRODUCTION','GROUP':'$GROUP','APP_OPTS':'$APP_OPTS','ENV':'release','JAVA_OPTS':'$JAVA_OPTS','EUREKA_OPTS':'$EUREKA_OPTS',"+'"TAG":"$TAG","WAR_NAME":"$WAR_NAME","EXPORT":""}')
        stringParam ('SHELL_NAME',"$SHELL_NAME")

    }
    steps {
      jumpserverHttpsBuilder {
      //调用jumpserver插件发布
            jpsURL('生产环境jps')
            appName("$PRODUCTION")   //记录jumpserver上发布的微服务名称
        	ecsSearchOption("$ECS_IP1")


        }
    }
}

freeStyleJob("$DEPLOY_JOB_NAME2") {
    logRotator(3, 3)
    parameters {
        password {
              name('JPS_PWD')
              defaultValue('')
              description('')
        }
        stringParam ('TAG',"")
        stringParam ('OLD_VERSION',"")
        stringParam ('WAR_NAME',"$WAR_NAME")
        textParam ('JPS_PARAMETER',"{'PRODUCTION':'$PRODUCTION','GROUP':'$GROUP','APP_OPTS':'$APP_OPTS','ENV':'release','JAVA_OPTS':'$JAVA_OPTS','EUREKA_OPTS':'$EUREKA_OPTS',"+'"TAG":"$TAG","WAR_NAME":"$WAR_NAME","EXPORT":""}')
        stringParam ('SHELL_NAME',"$SHELL_NAME")

    }
    steps {
      jumpserverHttpsBuilder {
      //调用jumpserver插件发布
            jpsURL('生产环境jps')
            appName("$PRODUCTION")   //记录jumpserver上发布的微服务名称
        	ecsSearchOption("$ECS_IP2")

        }
    }
}
freeStyleJob("$DEPLOY_JOB_NAME3") {
    logRotator(3, 3)
    parameters {
        password {
              name('JPS_PWD')
              defaultValue('')
              description('')
        }
        stringParam ('TAG',"")
        stringParam ('OLD_VERSION',"")
        stringParam ('WAR_NAME',"$WAR_NAME")
        textParam ('JPS_PARAMETER',"{'PRODUCTION':'$PRODUCTION','GROUP':'$GROUP','APP_OPTS':'$APP_OPTS','ENV':'release','JAVA_OPTS':'$JAVA_OPTS','EUREKA_OPTS':'$EUREKA_OPTS',"+'"TAG":"$TAG","WAR_NAME":"$WAR_NAME","EXPORT":""}')
        stringParam ('SHELL_NAME',"$SHELL_NAME")

    }
    steps {
      jumpserverHttpsBuilder {
      //调用jumpserver插件发布
            jpsURL('生产环境jps')
            appName("$PRODUCTION")   //记录jumpserver上发布的微服务名称
        	ecsSearchOption("$ECS_IP3")

        }
    }
}
pipelineJob("$PIPELINE_JOB_NAME") {
    logRotator(3, 3)
    parameters {
        password {
            name('JPS_PWD')
            defaultValue('')
            description('')
        }
        stringParam ('TAG',"")
        stringParam ('OLD_VERSION',"")
        textParam ('Description',"")
        stringParam ('BRANCH',"$BRANCH")
        stringParam ('WAR_NAME',"$WAR_NAME")
    }

  definition {
    cpsScm {
      scm {
        git {
          remote {
            url("https://github.com/Aaron1989/jenkins-groovy.git")
            branch("*/master")
            credentials("$Git_AUTH")
          }
        }
      }
      scriptPath("jenkins-pipelines/prodd/plugins/Prod-plugins-ms-hospital-Pipeline.groovy")
    }
  }
}
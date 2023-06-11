# Knowre Android Library

- 노리 프로젝트에 사용되는 공통 코드들을 모아두는 서브모듈 레퍼지토리입니다.

# Modules

- extension-standard
  - 순수 kotlin 관련 커스텀 확장함수 및 유틸 클래스 코드를 작성하는 모듈입니다.

- extension-android
  - 안드로이드 관련 커스텀 뷰 / 확장함수 및 유틸 클래스 코드를 작성하는 모듈입니다.
  - extension-standard 에 dependency 를 가지고 있습니다.

# Usage

- 해당 레파지토리가 서브모듈로 설정되어 있지 않은 레파지토리에서 임포트할 경우
  - 아래 명령어로 서브모듈을 super 레파지토리에 추가 (폴더명은 kal 로 설정)
  ```
  super> git submodule add https://github.com/Knowre-Dev/KnowreAndroidLibrary.git kal
  ```
  - 위처럼 하면 super 레파지토리 루트에 .gitsubmodule 파일이 생성됨. 해당 파일에서 맨 아랫줄에 ```branch = main``` 추가
    - branch 를 main 으로 설정할 경우 ```git submodule update --remote``` 실행 시 항상 main branch 의 최신 내용을 pull 하게 됨
  - 최상위 setting.gradle 에 아래 내용 추가(완료하고나면 IDE 가 해당 폴더들을 모듈로 인식하기 시작)
  ``` gradle
  include ':kal'
  include ':kal:app'
  include ':kal:extension-android'
  include ':kal:extension-standard'
  include ':kal:기타_kal_프로젝트에_생성된_모듈들'
  ```
  - kal:app 의 build.gradle 에는 buildType 이 release 밖에 없기 때문에, super 프로젝트의 buildType 과 불일치 할 경우 에러가 발생한다. 이를 해결하기 위해 super 프로젝트의 build.gradle 에 아래와 같은 buildType matchingFallbacks 추가.
  ``` gradle
  buildTypes {
        debugTest {
             /**
             * 만약 import 된 android module 이 debugTest buildType 을 가지고 있지 않을 경우 해당 모듈은 debug 로 빌드하도록 설정
             */
            matchingFallbacks = ['debug']
        }
        
        qa {
            matchingFallbacks = ['debug']
        }
    }
  ```
  - super 프로젝트의 app 수준의 gradle 에서 아래와 같이 서브모듈의 모듈을 선택적으로 임포트하여 사용
  ``` gradle 
  implementation(project(':kal:extension-android'))
  implementation(project(':kal:extension-standard'))
  ```

- 서브 모듈이 포함된 레포지토리를 클론하는 경우
  - 아래 명령어로 슈퍼 레포지토리를 clone
  ```
  android_project> git clone [URL_FOR_SOME_THING_SUPER_REPOSITRY_THAT_HAS_THIS_SUB_MODULE]
  android_project> cd [PROJECT_NAME]
  ```
  - 위처럼 땡겨왔을 경우 서브모듈 폴더 kal 안에는 아무 내용이 없음
  - 아래 명령어로 서브모듈에 대한 설정파일 생성(init) 및 서브 모듈안의 코드들 fetch 해옴(update)
  ```
  super> git submodule init
  super> git submodule update --remote
  ```

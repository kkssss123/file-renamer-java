# Java Image Renamer

이미지·미디어 파일을 자연 정렬한 순서대로 번호를 붙이고, 원본 폴더 구조를 유지해 복사하는 Java CLI 도구입니다.

> 이 저장소는 Java 프로토타입의 개발·개선 과정을 기록한 Legacy 프로젝트입니다.
> 후속 프로젝트는 [Rust 버전](https://github.com/kkssss123/rusty_shell.git)에서 확인할 수 있습니다.

## 프로젝트 배경

다수의 이미지 파일을 정리하면서 파일명을 하나씩 변경하는 반복 작업을 줄이고자 개발했습니다.

폴더별 파일을 일정한 규칙으로 정리하고, 원본을 유지하면서 결과를 별도 폴더에 생성하는 것을 목표로 했습니다. V1부터 V3까지 구현을 개선하며 파일 시스템 API 활용, 정렬 방식, 파일명 생성 로직을 다뤘습니다.

## 주요 기능

- **일괄 파일명 생성:** 시작 번호부터 순차적으로 번호 부여
- **자연 정렬:** 파일명의 숫자 구간을 수치로 비교
- **원본 보존:** 원본 파일을 변경하지 않고 별도 폴더에 복사
- **폴더 구조 유지:** 입력 폴더의 하위 디렉토리 구조를 출력 폴더에 재현
- **동적 번호 패딩:** 폴더별 파일 수에 따라 최소 3자리 또는 4자리 적용
- **미디어 파일 지원:** 이미지와 동영상 확장자 처리

### 지원 확장자

`.jpg`, `.jpeg`, `.webp`, `.png`, `.gif`, `.mp4`, `.mov`

확장자는 대소문자를 구분하지 않고 판별하며, 출력 파일에는 원래 확장자 표기를 유지합니다.

## 실행 예시

다음과 같은 파일을 입력하면:

```text
images/
└── trip/
    ├── photo1.jpg
    ├── photo2.jpg
    └── photo10.jpg
```

자연 정렬한 순서에 따라 번호를 붙여 복사합니다.

```text
output/
└── trip/
    ├── 001.jpg    ← photo1.jpg
    ├── 002.jpg    ← photo2.jpg
    └── 003.jpg    ← photo10.jpg
```

각 디렉토리의 번호는 지정한 시작 번호부터 독립적으로 부여됩니다.

## 사용 방법

### 실행 환경

- JDK 8 이상
- 별도 외부 라이브러리 없이 Java 표준 라이브러리 사용

### 준비

프로젝트 루트에 `images` 폴더를 만들고 처리할 파일을 넣습니다.

```text
project/
├── images/
│   └── 처리할 파일 및 하위 폴더
└── src/
```

### 컴파일

프로젝트 루트에서 실행합니다.

```bash
javac -d out src/Main.java src/fileutils/RenameImageFilesV3.java
```

### 실행

기본 시작 번호는 `1`입니다.

```bash
java -cp out Main
```

시작 번호를 직접 지정할 수도 있습니다.

```bash
java -cp out Main 100
```

입력은 실행 위치 기준 `./images`에서 읽으며, 결과는 `./output`에 생성됩니다.

## V1 → V3 개선 과정

### V1 — 파일명 변경 기능 구현

`Files.walkFileTree`로 하위 디렉토리를 순회하고, `Files.move`를 사용해 파일명을 변경하는 기본 기능을 구현했습니다.

- 디렉토리 순회 및 확장자 필터링
- 문자열 기준 파일명 정렬
- 파일명 앞에 `0`을 붙이는 패딩 처리

### V2 — 원본 보존과 디렉토리 구조 복제

원본 파일을 직접 변경하는 방식에서, 별도 출력 폴더에 복사하는 방식으로 전환했습니다.

- `Files.copy`를 사용한 원본 보존
- `Path.relativize()`로 입력 폴더 기준 상대 경로 계산
- `Files.createDirectories()`로 출력 디렉토리 구조 생성
- 이미지 외에 GIF·동영상 확장자 지원
- 파일명을 순차적인 번호로 생성

### V3 — 자연 정렬과 파일명 생성 개선

파일명의 숫자를 기준으로 정렬하고, 파일 수에 맞춰 번호 자릿수를 결정하도록 개선했습니다.

- **자연 정렬:** `1.jpg → 2.jpg → 10.jpg` 순서로 처리
- **숫자 비교:** 연속된 숫자 구간을 `BigInteger`로 변환해 비교
- **가변 자릿수:** 파일 1~999개는 최소 3자리, 1,000~9,999개는 최소 4자리 적용
- **번호 유지:** 지정한 자릿수를 넘는 번호도 앞자리를 잘라내지 않고 유지
- **시작 번호 지정:** 실행 인자로 번호 설정

## 주요 구현

### 상대 경로를 이용한 폴더 구조 유지

입력 루트에서 각 디렉토리까지의 상대 경로를 구한 뒤, 출력 루트에 결합합니다.

```java
Path relativeDir = Paths.get(baseDir).relativize(dir);
Path targetDir = Paths.get(destDir).resolve(relativeDir);
Files.createDirectories(targetDir);
```

이 방식으로 폴더 깊이나 이름을 별도로 처리하지 않고 디렉토리 구조를 재현했습니다.

### 숫자 구간을 비교하는 자연 정렬

파일명을 순회하며 양쪽 문자가 숫자이면 연속된 숫자 구간을 추출합니다. 추출한 값을 `BigInteger`로 비교해 일반 문자열 정렬과 다른 숫자 순서를 적용합니다.

```text
문자열 정렬: photo1.jpg → photo10.jpg → photo2.jpg
자연 정렬:   photo1.jpg → photo2.jpg → photo10.jpg
```

### 최소 자릿수를 지정하는 파일명 생성

```java
String.format("%0" + padWidth + "d%s", startNum, extension);
```

`padWidth`는 최소 출력 폭으로 사용됩니다. 번호가 해당 폭보다 길어져도 숫자를 그대로 유지합니다.

## 프로젝트 구조

```text
src/
├── Main.java
└── fileutils/
    ├── RenameImageFilesV1.java
    ├── RenameImageFilesV2.java
    └── RenameImageFilesV3.java
```

| 파일 | 역할 |
|---|---|
| `Main.java` | 경로 및 시작 번호 설정, V3 실행 |
| `RenameImageFilesV1.java` | 초기 파일명 변경 구현 |
| `RenameImageFilesV2.java` | 파일 복사 및 디렉토리 구조 유지 |
| `RenameImageFilesV3.java` | 자연 정렬 및 번호 생성 개선 |

## 개발을 통해 얻은 경험

- Java NIO를 활용한 디렉토리 순회와 파일 복사
- 상대 경로 계산을 통한 디렉토리 구조 재현
- 문자열 정렬과 자연 정렬의 차이 이해 및 비교 로직 구현
- 원본을 직접 변경하는 방식에서 결과를 별도로 생성하는 방식으로 전환
- 실제 사용 과정에서 발견한 요구사항을 버전별 개선으로 연결

## 후속 프로젝트

Java 버전에서 구현한 파일 정리 기능을 바탕으로, 사용 흐름을 개선하기 위한 Rust 버전을 개발했습니다.

- [Rust 파일 정리 도구](https://github.com/kkssss123/rusty_shell.git)

이 저장소는 초기 구현부터 개선까지의 과정을 보여주는 개발 기록으로 유지합니다.

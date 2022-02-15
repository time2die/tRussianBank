FROM gradle:6.7.1-jdk8-openj9

COPY  . .

RUN gradle build --no-daemon

ENTRYPOINT ["gradle", "run", "--no-daemon"]

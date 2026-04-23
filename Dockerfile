FROM ubuntu:latest
LABEL authors="malbasini"

ENTRYPOINT ["top", "-b"]
FROM openjdk:11-jre-slim

RUN apt-get update && apt-get install -y curl gnupg && \
    curl -fL "https://github.com/sbt/sbt/releases/download/v1.8.2/sbt-1.8.2.tgz" | tar xz -C /usr/local && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy your Scala project files
COPY . /app
WORKDIR /app

# Build your Scala project
RUN sbt compile

# Expose the port your application runs on
EXPOSE 8080

# Command to run your application
CMD ["sbt", "run"]

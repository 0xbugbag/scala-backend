FROM openjdk:11-jre-slim

# Install dependencies
RUN apt-get update && apt-get install -y \
    curl \
    gnupg \
    && rm -rf /var/lib/apt/lists/*

# Add the sbt repository and install sbt from the official repository
RUN echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list \
    && echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list \
    && curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2E7489F70127B2F7" | tee /etc/apt/trusted.gpg.d/sbt.asc \
    && apt-get update \
    && apt-get install -y sbt

# Copy your Scala project files
COPY . /app
WORKDIR /app

# Build your Scala project
RUN sbt compile

# Expose the port your application runs on
EXPOSE 8080

# Command to run your application
CMD ["sbt", "run"]

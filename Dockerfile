FROM openjdk:11

# Install curl and other dependencies
RUN apt-get update && \
    apt-get install -y curl gnupg && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add && \
    apt-get update && \
    apt-get install -y sbt && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy project files
COPY project project
COPY build.sbt .
COPY src src

# Set SBT_OPTS to limit memory usage
ENV SBT_OPTS="-Xmx256m -Xms64m"

# Build the project
RUN sbt clean compile stage

# Expose port if needed
EXPOSE 9000

# Command to run the application with memory constraints
CMD ["target/universal/stage/bin/scala-backend", "-Dconfig.resource=production.conf"]

ENV JAVA_OPTS="-Xmx256m -Xms64m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:MaxGCPauseMillis=100"

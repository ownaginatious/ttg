JENKINS_HOME=/var/lib/jenkins
tar --exclude "${JENKINS_HOME}/identity.key.enc" \
    --exclude "${JENKINS_HOME}/secrets" \
    --exclude "${JENKINS_HOME}/jobs/*/workspace/*" \
    --exclude "*/.*" \
    -cvzf /home/dillon/jenkins.tar.gz "${JENKINS_HOME}/"

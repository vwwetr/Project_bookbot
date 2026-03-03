pipeline {
    agent { label 'host' }

    environment {
        ANSIBLE_BIN = tool name: 'Ansible_2.19.3', type: 'ansible'
        PATH = "${ANSIBLE_HOME}/bin:${env.PATH}"
    }

    options {
        timestamps()
    }

    stages {
        stage('Checkout git repo') {
            steps {
                checkout scm
            }
        }

        stage('Validate ansible playbook') {
            steps {
                dir('Project_bookbot/Ansible') {
                    sh 'ansible-playbook --syntax-check site.yml'
                }
            }
        }

        stage('Cluster users set up') {
            steps {
                dir('Project_bookbot/Ansible') {
                    withCredentials([
                        file(credentialsId: 'ansible-vault-pass', variable: 'VAULT_PASS_FILE'),
                        sshUserPrivateKey(
                        credentialsId: 'vagrant-ssh',
                        keyFileVariable: 'VAGRANT_KEY_FILE',
                        usernameVariable: 'VAGRANT_USER'
                        )
                    ]) {
                        sh '''
                            ansible-playbook site.yml \
                            --tags users \
                            -u vagrant \
                            --private-key "$VAGRANT_KEY_FILE" \
                            --vault-password-file "$VAULT_PASS_FILE"
                        '''
                    }
                }
            }
        }

        stage('Install Postgres') {
            steps {
                dir('Project_bookbot/Ansible') {
                    withCredentials([
                        file(credentialsId: 'ansible-vault-pass', variable: 'VAULT_PASS_FILE'),
                    ]) {
                        sh '''
                            ansible-playbook site.yml \
                            --tags pg_install \
                            --vault-password-file "$VAULT_PASS_FILE"
                        '''
                    }
                }
            }
        }

        stage('Create and set up db') {
            steps {
                dir('Project_bookbot/Ansible') {
                    withCredentials([
                        file(credentialsId: 'ansible-vault-pass', variable: 'VAULT_PASS_FILE'),
                    ]) {
                        sh '''
                            ansible-playbook site.yml \
                            --tags db_create \
                            --vault-password-file "$VAULT_PASS_FILE"
                        '''
                    }
                }
            }
        }

        stage('Set up and run pg_dump') {
            steps {
                dir('Project_bookbot/Ansible') {
                    withCredentials([
                        file(credentialsId: 'ansible-vault-pass', variable: 'VAULT_PASS_FILE'),
                    ]) {
                        sh '''
                            ansible-playbook site.yml \
                            --tags db_backup \
                            --vault-password-file "$VAULT_PASS_FILE"
                        '''
                    }
                }
            }
        }

        stage('Deploy app in k8s') {
            steps {
                dir('Project_bookbot/Ansible') {
                    withCredentials([
                        file(credentialsId: 'ansible-vault-pass', variable: 'VAULT_PASS_FILE'),
                        file(credentialsId: 'app_secrets',        variable: 'APP_SECRETS_FILE')
                    ]) {
                        sh '''
                        ansible-playbook site.yml \
                            --tags app_k8s \
                            --vault-password-file "$VAULT_PASS_FILE"
                            --extra-vars "app_secrets_file=$APP_SECRETS_FILE"
                        '''
                    }
                }
            }
        }
    }
}
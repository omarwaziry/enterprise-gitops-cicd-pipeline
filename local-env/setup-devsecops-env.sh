#!/usr/bin/env bash

# ==============================================================================
# Enterprise GitOps CI/CD Pipeline - Local Dev Environment Setup Script
# ==============================================================================
#
# Constraints: 8GB RAM, 2 CPUs.
# Est. RAM consumption: ~6.5 - 7GB. Swap space (6GB active) will handle spikes.
#
# RUNNING THIS SCRIPT:
# System tuning (sysctl, firewall) requires sudo.
# Run: sudo ./setup-devsecops-env.sh
# ==============================================================================

set -euo pipefail

# Text formatting helper variables
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}======================================================================${NC}"
echo -e "${GREEN} Starting Enterprise DevSecOps Lab Environment Setup (Cross-Distro)   ${NC}"
echo -e "${GREEN}======================================================================${NC}"

# Check for root privilege
if [ "$EUID" -ne 0 ]; then
  echo -e "${RED}Error: System kernel tuning and firewall configuration require root privileges.${NC}"
  echo -e "Please run this script with sudo: ${YELLOW}sudo $0${NC}"
  exit 1
fi

# Store the non-root caller username (since Minikube cannot run as root)
SUDO_CALLER="${SUDO_USER:-}"
if [ -z "$SUDO_CALLER" ] || [ "$SUDO_CALLER" = "root" ]; then
  echo -e "${RED}Warning: Could not detect the underlying non-root user. Minikube configuration will need manual startup.${NC}"
fi

# ==========================================
# 1. Host Kernel Performance Tuning (SonarQube)
# ==========================================
echo -e "\n${GREEN}[1/5] Configuring Host Kernel Limits for Elasticsearch...${NC}"

# Configure sysctl limits dynamically
echo "Applying vm.max_map_count=524288"
sysctl -w vm.max_map_count=524288
echo "Applying fs.file-max=131072"
sysctl -w fs.file-max=131072

# Persist configurations across reboot
SYSCTL_CONF="/etc/sysctl.d/99-sonarqube-es.conf"
echo "Persisting kernel configuration to $SYSCTL_CONF"
cat <<EOF > "$SYSCTL_CONF"
# SonarQube Elasticsearch requirements
vm.max_map_count=524288
fs.file-max=131072
EOF

# ==========================================
# 2. Dynamic Firewall Configuration (Ports 8080, 9000, 80, 443)
# ==========================================
echo -e "\n${GREEN}[2/5] Configuring Host Firewall (Ports 8080, 9000, 80, 443)...${NC}"
if command -v firewall-cmd &>/dev/null && systemctl is-active --quiet firewalld; then
  echo "Detected firewalld (RHEL/CentOS/Fedora). Applying rules..."
  firewall-cmd --permanent --add-port=8080/tcp
  firewall-cmd --permanent --add-port=9000/tcp
  firewall-cmd --permanent --add-port=80/tcp
  firewall-cmd --permanent --add-port=443/tcp
  firewall-cmd --reload
  echo -e "${GREEN}Firewalld rules updated successfully.${NC}"
elif command -v ufw &>/dev/null && ufw status | grep -q "active"; then
  echo "Detected UFW (Ubuntu/Debian). Applying rules..."
  ufw allow 8080/tcp
  ufw allow 9000/tcp
  ufw allow 80/tcp
  ufw allow 443/tcp
  ufw reload
  echo -e "${GREEN}UFW rules updated successfully.${NC}"
else
  echo -e "${YELLOW}No active firewalld or UFW detected. Please ensure ports 8080 (Jenkins), 9000 (SonarQube), 80, and 443 are open in your networking/security groups.${NC}"
fi

# ==========================================
# 3. Spin up Jenkins & SonarQube (Host Docker)
# ==========================================
echo -e "\n${GREEN}[3/5] Starting Host Containers (Jenkins & SonarQube)...${NC}"

# Check for docker-compose vs docker compose
COMPOSE_CMD=""
if docker compose version &>/dev/null; then
  COMPOSE_CMD="docker compose"
elif docker-compose --version &>/dev/null; then
  COMPOSE_CMD="docker-compose"
else
  echo -e "${RED}Error: Docker Compose is not installed on this VM. Please install it first.${NC}"
  exit 1
fi

echo -e "Using command: ${YELLOW}$COMPOSE_CMD${NC}"
$COMPOSE_CMD up -d

# ==========================================
# 4. Spin up Minikube Cluster
# ==========================================
echo -e "\n${GREEN}[4/5] Preparing Minikube Cluster...${NC}"

if [ -n "$SUDO_CALLER" ] && [ "$SUDO_CALLER" != "root" ]; then
  # Run minikube commands as the non-root user
  echo -e "Starting Minikube cluster as user: ${YELLOW}$SUDO_CALLER${NC}"
  echo -e "Memory allocation: ${YELLOW}2560MB (2.5GB)${NC}, CPUs: ${YELLOW}2${NC}"
  
  # Configure minikube parameters
  su - "$SUDO_CALLER" -c "minikube start --driver=docker --memory=2560 --cpus=2"
  
  # Enable Nginx Ingress Addon
  echo "Enabling Ingress addon in Minikube..."
  su - "$SUDO_CALLER" -c "minikube addons enable ingress"
else
  echo -e "${YELLOW}Skipping automatic Minikube start since run as root without SUDO_USER.${NC}"
  echo -e "Please start Minikube manually as a NON-ROOT user using:"
  echo -e "  ${YELLOW}minikube start --driver=docker --memory=2560 --cpus=2${NC}"
  echo -e "  ${YELLOW}minikube addons enable ingress${NC}"
fi

# ==========================================
# 5. Deploy Argo CD to Minikube
# ==========================================
echo -e "\n${GREEN}[5/5] Deploying GitOps Controller (Argo CD)...${NC}"

if [ -n "$SUDO_CALLER" ] && [ "$SUDO_CALLER" != "root" ]; then
  # Create namespace and deploy manifests
  su - "$SUDO_CALLER" -c "kubectl create namespace argocd || true"
  su - "$SUDO_CALLER" -c "kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml"
  
  echo -e "${GREEN}Waiting for Argo CD components to initialize...${NC}"
  su - "$SUDO_CALLER" -c "kubectl rollout status deployment/argocd-dex-server -n argocd --timeout=60s || true"
  
  # Fetch temporary admin password
  ARGOCD_PASSWORD=$(su - "$SUDO_CALLER" -c "kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath='{.data.password}'" 2>/dev/null | base64 -d 2>/dev/null || echo "PENDING_INITIALIZATION")
  
  echo -e "\n${GREEN}======================================================================${NC}"
  echo -e "${GREEN} Deployment Environment Setup Completed!${NC}"
  echo -e "${GREEN}======================================================================${NC}"
  echo -e "Host Services:"
  echo -e "  - ${YELLOW}Jenkins CI${NC}:        http://YOUR_VM_IP:8080"
  echo -e "  - ${YELLOW}SonarQube UI${NC}:      http://YOUR_VM_IP:9000"
  echo -e "  - ${YELLOW}Local App Port${NC}:    http://devops-showcase.local (Add mapping in /etc/hosts to VM IP)"
  echo -e "\nGitOps Controller (Argo CD):"
  echo -e "  - Access UI via port forwarding: ${YELLOW}kubectl port-forward svc/argocd-server -n argocd 8081:443${NC}"
  echo -e "  - Initial Username: ${YELLOW}admin${NC}"
  if [ "$ARGOCD_PASSWORD" != "PENDING_INITIALIZATION" ]; then
    echo -e "  - Initial Password: ${YELLOW}$ARGOCD_PASSWORD${NC}"
  else
    echo -e "  - Initial Password: run '${YELLOW}kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath=\"{.data.password}\" | base64 -d${NC}' once pods are ready."
  fi
else
  echo -e "${YELLOW}Minikube skipped. Once you start Minikube manually, deploy Argo CD with:${NC}"
  echo -e "  ${YELLOW}kubectl create namespace argocd${NC}"
  echo -e "  ${YELLOW}kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml${NC}"
fi

echo -e "\n${YELLOW}Memory Optimization Tips:${NC}"
echo -e "  1. Minimize concurrent builds in Jenkins (disabled by default in Jenkinsfile)."
echo -e "  2. Clean build caches occasionally: 'docker system prune -a --volumes'."
echo -e "  3. Monitor active memory with: 'free -m' or 'htop'."
echo -e "${GREEN}======================================================================${NC}"

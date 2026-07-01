package com.example.devopsproject.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
import java.time.Instant;

@RestController
public class HomeController {

    @GetMapping(value = "/", produces = "text/html")
    public String index() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>GitOps CI/CD Showcase App</title>\n" +
                "    <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n" +
                "    <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>\n" +
                "    <link href=\"https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;800&display=swap\" rel=\"stylesheet\">\n" +
                "    <style>\n" +
                "        :root {\n" +
                "            --bg-color: #0b0f19;\n" +
                "            --card-bg: rgba(22, 28, 45, 0.7);\n" +
                "            --primary: #3b82f6;\n" +
                "            --primary-glow: rgba(59, 130, 246, 0.3);\n" +
                "            --success: #10b981;\n" +
                "            --success-glow: rgba(16, 185, 129, 0.2);\n" +
                "            --text-main: #f8fafc;\n" +
                "            --text-muted: #94a3b8;\n" +
                "            --border: rgba(255, 255, 255, 0.08);\n" +
                "        }\n" +
                "        \n" +
                "        * {\n" +
                "            box-sizing: border-box;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "        \n" +
                "        body {\n" +
                "            font-family: 'Outfit', sans-serif;\n" +
                "            background-color: var(--bg-color);\n" +
                "            color: var(--text-main);\n" +
                "            min-height: 100vh;\n" +
                "            display: flex;\n" +
                "            flex-direction: column;\n" +
                "            align-items: center;\n" +
                "            justify-content: center;\n" +
                "            padding: 2rem 1.5rem;\n" +
                "            overflow-x: hidden;\n" +
                "            background-image: \n" +
                "                radial-gradient(circle at 10% 20%, rgba(59, 130, 246, 0.1) 0%, transparent 40%),\n" +
                "                radial-gradient(circle at 90% 80%, rgba(16, 185, 129, 0.08) 0%, transparent 45%);\n" +
                "        }\n" +
                "        \n" +
                "        .container {\n" +
                "            max-width: 900px;\n" +
                "            width: 100%;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        \n" +
                "        header {\n" +
                "            margin-bottom: 3rem;\n" +
                "            animation: fadeInDown 0.8s ease-out;\n" +
                "        }\n" +
                "        \n" +
                "        .badge {\n" +
                "            display: inline-flex;\n" +
                "            align-items: center;\n" +
                "            gap: 0.5rem;\n" +
                "            background: var(--success-glow);\n" +
                "            color: var(--success);\n" +
                "            padding: 0.4rem 1rem;\n" +
                "            border-radius: 9999px;\n" +
                "            font-size: 0.85rem;\n" +
                "            font-weight: 600;\n" +
                "            border: 1px solid rgba(16, 185, 129, 0.3);\n" +
                "            margin-bottom: 1rem;\n" +
                "            text-transform: uppercase;\n" +
                "            letter-spacing: 0.05em;\n" +
                "        }\n" +
                "        \n" +
                "        .pulse-dot {\n" +
                "            width: 8px;\n" +
                "            height: 8px;\n" +
                "            background-color: var(--success);\n" +
                "            border-radius: 50%;\n" +
                "            animation: pulse 1.5s infinite;\n" +
                "        }\n" +
                "        \n" +
                "        h1 {\n" +
                "            font-size: 2.8rem;\n" +
                "            font-weight: 800;\n" +
                "            background: linear-gradient(135deg, #fff 30%, #a5f3fc 100%);\n" +
                "            -webkit-background-clip: text;\n" +
                "            -webkit-text-fill-color: transparent;\n" +
                "            margin-bottom: 0.75rem;\n" +
                "            letter-spacing: -0.02em;\n" +
                "        }\n" +
                "        \n" +
                "        .subtitle {\n" +
                "            color: var(--text-muted);\n" +
                "            font-size: 1.15rem;\n" +
                "            max-width: 600px;\n" +
                "            margin: 0 auto;\n" +
                "            line-height: 1.6;\n" +
                "        }\n" +
                "        \n" +
                "        .card {\n" +
                "            background: var(--card-bg);\n" +
                "            backdrop-filter: blur(12px);\n" +
                "            -webkit-backdrop-filter: blur(12px);\n" +
                "            border: 1px solid var(--border);\n" +
                "            border-radius: 1.25rem;\n" +
                "            padding: 2.5rem;\n" +
                "            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);\n" +
                "            margin-bottom: 2.5rem;\n" +
                "            animation: fadeInUp 0.8s ease-out 0.2s both;\n" +
                "            text-align: left;\n" +
                "        }\n" +
                "        \n" +
                "        .grid {\n" +
                "            display: grid;\n" +
                "            grid-template-columns: repeat(2, 1fr);\n" +
                "            gap: 1.5rem;\n" +
                "            margin-bottom: 2rem;\n" +
                "        }\n" +
                "        \n" +
                "        .info-item {\n" +
                "            background: rgba(255, 255, 255, 0.02);\n" +
                "            border: 1px solid rgba(255, 255, 255, 0.04);\n" +
                "            padding: 1.25rem;\n" +
                "            border-radius: 0.75rem;\n" +
                "            transition: transform 0.2s ease, border-color 0.2s ease;\n" +
                "        }\n" +
                "        \n" +
                "        .info-item:hover {\n" +
                "            transform: translateY(-2px);\n" +
                "            border-color: rgba(59, 130, 246, 0.2);\n" +
                "        }\n" +
                "        \n" +
                "        .info-label {\n" +
                "            font-size: 0.85rem;\n" +
                "            color: var(--text-muted);\n" +
                "            text-transform: uppercase;\n" +
                "            letter-spacing: 0.05em;\n" +
                "            margin-bottom: 0.25rem;\n" +
                "        }\n" +
                "        \n" +
                "        .info-value {\n" +
                "            font-size: 1.1rem;\n" +
                "            font-weight: 600;\n" +
                "            color: var(--text-main);\n" +
                "        }\n" +
                "        \n" +
                "        .pipeline-preview {\n" +
                "            border-top: 1px solid var(--border);\n" +
                "            padding-top: 2rem;\n" +
                "            margin-top: 2rem;\n" +
                "        }\n" +
                "        \n" +
                "        .pipeline-title {\n" +
                "            font-size: 1.1rem;\n" +
                "            font-weight: 600;\n" +
                "            margin-bottom: 1.25rem;\n" +
                "            display: flex;\n" +
                "            align-items: center;\n" +
                "            gap: 0.5rem;\n" +
                "        }\n" +
                "        \n" +
                "        .pipeline-steps {\n" +
                "            display: flex;\n" +
                "            justify-content: space-between;\n" +
                "            align-items: center;\n" +
                "            position: relative;\n" +
                "            overflow-x: auto;\n" +
                "            padding: 1rem 0;\n" +
                "        }\n" +
                "        \n" +
                "        .pipeline-steps::before {\n" +
                "            content: '';\n" +
                "            position: absolute;\n" +
                "            top: 50%;\n" +
                "            left: 0;\n" +
                "            right: 0;\n" +
                "            height: 2px;\n" +
                "            background: rgba(255, 255, 255, 0.08);\n" +
                "            z-index: 1;\n" +
                "            transform: translateY(-50%);\n" +
                "        }\n" +
                "        \n" +
                "        .step {\n" +
                "            position: relative;\n" +
                "            z-index: 2;\n" +
                "            background: #111827;\n" +
                "            border: 2px solid rgba(255, 255, 255, 0.15);\n" +
                "            color: var(--text-muted);\n" +
                "            width: 44px;\n" +
                "            height: 44px;\n" +
                "            border-radius: 50%;\n" +
                "            display: flex;\n" +
                "            align-items: center;\n" +
                "            justify-content: center;\n" +
                "            font-weight: 600;\n" +
                "            font-size: 0.95rem;\n" +
                "            transition: all 0.3s ease;\n" +
                "            cursor: default;\n" +
                "        }\n" +
                "        \n" +
                "        .step:hover {\n" +
                "            transform: scale(1.15);\n" +
                "            border-color: var(--primary);\n" +
                "            color: var(--primary);\n" +
                "            box-shadow: 0 0 15px var(--primary-glow);\n" +
                "        }\n" +
                "        \n" +
                "        .step.active {\n" +
                "            border-color: var(--success);\n" +
                "            background: var(--success-glow);\n" +
                "            color: var(--success);\n" +
                "            box-shadow: 0 0 15px rgba(16, 185, 129, 0.3);\n" +
                "        }\n" +
                "        \n" +
                "        .step-label {\n" +
                "            position: absolute;\n" +
                "            top: 52px;\n" +
                "            left: 50%;\n" +
                "            transform: translateX(-50%);\n" +
                "            white-space: nowrap;\n" +
                "            font-size: 0.75rem;\n" +
                "            font-weight: 500;\n" +
                "            color: var(--text-muted);\n" +
                "        }\n" +
                "        \n" +
                "        .step.active .step-label {\n" +
                "            color: var(--success);\n" +
                "            font-weight: 600;\n" +
                "        }\n" +
                "        \n" +
                "        footer {\n" +
                "            color: rgba(255, 255, 255, 0.25);\n" +
                "            font-size: 0.85rem;\n" +
                "            animation: fadeIn 1.2s ease-out both;\n" +
                "        }\n" +
                "        \n" +
                "        footer a {\n" +
                "            color: var(--text-muted);\n" +
                "            text-decoration: none;\n" +
                "            transition: color 0.2s;\n" +
                "        }\n" +
                "        \n" +
                "        footer a:hover {\n" +
                "            color: var(--primary);\n" +
                "        }\n" +
                "        \n" +
                "        @keyframes pulse {\n" +
                "            0% { transform: scale(0.9); opacity: 0.6; }\n" +
                "            50% { transform: scale(1.1); opacity: 1; }\n" +
                "            100% { transform: scale(0.9); opacity: 0.6; }\n" +
                "        }\n" +
                "        \n" +
                "        @keyframes fadeInDown {\n" +
                "            from { opacity: 0; transform: translateY(-20px); }\n" +
                "            to { opacity: 1; transform: translateY(0); }\n" +
                "        }\n" +
                "        \n" +
                "        @keyframes fadeInUp {\n" +
                "            from { opacity: 0; transform: translateY(20px); }\n" +
                "            to { opacity: 1; transform: translateY(0); }\n" +
                "        }\n" +
                "        \n" +
                "        @keyframes fadeIn {\n" +
                "            from { opacity: 0; }\n" +
                "            to { opacity: 0.5; }\n" +
                "        }\n" +
                "        \n" +
                "        @media (max-width: 640px) {\n" +
                "            .grid {\n" +
                "                grid-template-columns: 1fr;\n" +
                "            }\n" +
                "            h1 {\n" +
                "                font-size: 2.2rem;\n" +
                "            }\n" +
                "            .pipeline-steps {\n" +
                "                justify-content: flex-start;\n" +
                "                gap: 3.5rem;\n" +
                "            }\n" +
                "            .pipeline-steps::before {\n" +
                "                display: none;\n" +
                "            }\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <header>\n" +
                "            <div class=\"badge\">\n" +
                "                <div class=\"pulse-dot\"></div>\n" +
                "                Live & Synced\n" +
                "            </div>\n" +
                "            <h1>GitOps Deployment Dashboard</h1>\n" +
                "            <p class=\"subtitle\">This microservice is automatically built, containerized, and deployed to Kubernetes using Jenkins, Docker, and Argo CD GitOps reconcilers.</p>\n" +
                "        </header>\n" +
                "        \n" +
                "        <div class=\"card\">\n" +
                "            <div class=\"grid\">\n" +
                "                <div class=\"info-item\">\n" +
                "                    <div class=\"info-label\">Service Status</div>\n" +
                "                    <div class=\"info-value\" style=\"color: var(--success);\">Healthy (UP)</div>\n" +
                "                </div>\n" +
                "                <div class=\"info-item\">\n" +
                "                    <div class=\"info-label\">Framework</div>\n" +
                "                    <div class=\"info-value\">Spring Boot 3.2.4</div>\n" +
                "                </div>\n" +
                "                <div class=\"info-item\">\n" +
                "                    <div class=\"info-label\">Active Environment</div>\n" +
                "                    <div class=\"info-value\">Kubernetes (K8s) Pod</div>\n" +
                "                </div>\n" +
                "                <div class=\"info-item\">\n" +
                "                    <div class=\"info-label\">Version Tag</div>\n" +
                "                    <div class=\"info-value\">v1.0.0</div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class=\"pipeline-preview\">\n" +
                "                <div class=\"pipeline-title\">\n" +
                "                    <svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"var(--primary)\" stroke-width=\"2.5\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><path d=\"M22 11.08V12a10 10 0 1 1-5.93-9.14\"></path><polyline points=\"22 4 12 14.01 9 11.01\"></polyline></svg>\n" +
                "                    Pipeline Progression Stages\n" +
                "                </div>\n" +
                "                <div class=\"pipeline-steps\">\n" +
                "                    <div class=\"step active\">\n" +
                "                        Git\n" +
                "                        <div class=\"step-label\">Source Code</div>\n" +
                "                    </div>\n" +
                "                    <div class=\"step active\">\n" +
                "                        CI\n" +
                "                        <div class=\"step-label\">Jenkins</div>\n" +
                "                    </div>\n" +
                "                    <div class=\"step active\">\n" +
                "                        mvn\n" +
                "                        <div class=\"step-label\">SonarQube</div>\n" +
                "                    </div>\n" +
                "                    <div class=\"step active\">\n" +
                "                        🐳\n" +
                "                        <div class=\"step-label\">DockerHub</div>\n" +
                "                    </div>\n" +
                "                    <div class=\"step active\">\n" +
                "                        ops\n" +
                "                        <div class=\"step-label\">Manifests Repo</div>\n" +
                "                    </div>\n" +
                "                    <div class=\"step active\">\n" +
                "                        CD\n" +
                "                        <div class=\"step-label\">Argo CD</div>\n" +
                "                    </div>\n" +
                "                    <div class=\"step active\">\n" +
                "                        k8s\n" +
                "                        <div class=\"step-label\">Kubernetes</div>\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <footer>\n" +
                "            Designed and developed as a DevOps Portfolio Showcase Project &bull; <a href=\"/api/info\" target=\"_blank\">View JSON Info API</a>\n" +
                "        </footer>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    @GetMapping(value = "/api/info", produces = "application/json")
    public Map<String, Object> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("status", "UP");
        info.put("service", "devops-showcase-app");
        info.put("framework", "Spring Boot");
        info.put("version", "1.0.0");
        info.put("environment", "Kubernetes Pod");
        info.put("timestamp", Instant.now().toString());
        
        Map<String, String> healthDetails = new HashMap<>();
        healthDetails.put("db", "healthy");
        healthDetails.put("disk", "healthy");
        healthDetails.put("memory", "healthy");
        info.put("healthDetails", healthDetails);
        
        return info;
    }
}

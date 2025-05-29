<template>
    <div class="main-container">
        <!-- 왼쪽 브랜딩 -->
        <div class="brand-section">
            <div class="brand-content">
                <div class="brand-logo">
                    <h1>Brand Logo</h1>
                </div>
                
                <h2 class="brand-title">
                    당신의 이미지,<br/>
                    즉시 분석하다
                </h2>
                
                <p class="brand-subtitle">
                    AI와 함께하는 스마트한 협업 도구.<br />
                    이미지를 업로드하면, 복잡한 과정 없이 간편하게 결과를 확인하세요.
                </p>
                
                <div class="brand-features">
                    <div class="feature-item">
                        <div class="feature-icon">✓</div>
                        <div class="feature-text">AI 이미지 분석 자동화</div>
                    </div>
                    <div class="feature-item">
                        <div class="feature-icon">✓</div>
                        <div class="feature-text">이미지 업로드만으로 자동 분석</div>
                    </div>
                    <div class="feature-item">
                        <div class="feature-icon">✓</div>
                        <div class="feature-text">빠른 처리 속도와 정확한 결과</div>
                    </div>
                    <div class="feature-item">
                        <div class="feature-icon">✓</div>
                        <div class="feature-text">팀 프로젝트 통합 관리</div>
                    </div>
                </div>
                
                <div class="brand-stats">
                    <div class="stat-item">
                        <div class="stat-number">100+</div>
                        <div class="stat-label">이미지 분석 횟수</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-number">100+</div>
                        <div class="stat-label">생성된 코드</div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-number">99.9%</div>
                        <div class="stat-label">서비스 안정성</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 오른쪽 로그인 폼 -->
        <div class="login-section">
            <div class="login-container">
                <div class="login-header">
                    <h1 class="login-title">Welcome back</h1>
                    <p class="login-subtitle">계정에 로그인하여 계속하세요</p>
                </div>

                <!-- 에러/성공 메시지 -->
                <div v-if="errorMessage" class="alert alert-error">
                    {{ errorMessage }}
                </div>
                <!-- <div v-if="successMessage" class="alert alert-success">
                    {{ successMessage }}
                </div> -->

                <!-- 로그인 폼 -->
                <form @submit.prevent="login">
                    <div class="form-group">
                        <label class="form-label" for="loginId">아이디</label>
                        <input
                            type="text" id="loginId" v-model="loginId"
                            class="form-control" placeholder="아이디를 입력해주세요"
                            required />
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="password">비밀번호</label>
                        <div class="password-group">
                            <input type="password" id="password" class="form-control"
                            v-model="password" placeholder="비밀번호를 입력해주세요" required />
                            <button type="button" class="password-toggle" @click="togglePasswordVisibility">
                                <i class="bi bi-eye-slash-fill"></i>
                            </button>
                        </div>
                    </div>

                    <!-- 옵션들 -->
                    <div class="form-options">
                        <label class="remember-me">
                            <input type="checkbox" id="rememberMe" v-model="rememberMe">
                            로그인 상태 유지
                        </label>
                        <a href="#" class="forgot-link">비밀번호를 잊으셨나요?</a>
                    </div>

                    <!-- 로그인 버튼 -->
                    <button type="submit" class="login-btn" :disabled="isLoading">
                        <span v-if="!isLoading">로그인</span>
                        <span v-else class="loading"></span>
                    </button>
                </form>

        
                <div class="divider">
                    <span>또는</span>
                </div>

                <!-- 소셜 로그인 -->
                <div class="social-login">
                    <button class="social-btn" @click="socialLogin('google')">
                        <div class="google-icon">G</div>
                        <span>Google로 계속하기</span>
                    </button>
                    
                    <button class="social-btn" @click="socialLogin('github')">
                        <span>🐙 GitHub로 계속하기</span>
                    </button>
                </div>

                <!-- 회원가입 링크 -->
                <div class="signup-section">
                    아직 계정이 없으신가요?
                    <a href="#" class="signup-link" @click="goToSignup">회원가입</a>
                </div>
            </div>
        </div>
    </div>
</template>


<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const loginId = ref('')
const password = ref('')
const rememberMe = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const login = async () => {
    if (!loginId.value || !password.value) {
        errorMessage.value = '이메일과 비밀번호를 입력해주세요.'
        return
    }
    
    errorMessage.value = ''
    successMessage.value = ''

  try {
    console.log('로그인 시도:', loginId.value)

    // proxy를 통한 spring boot 요청
    const res = await axios.post('/api/v1/login', {
      loginId: loginId.value,
      password: password.value
    })

    console.log('로그인 성공:', res.data)
    successMessage.value = '로그인에 성공했습니다.'
 

    localStorage.setItem('token', res.data.token)
    router.push('/main') // 로그인 성공 시 메인 페이지로 이동
  } catch (e) {
    errorMessage.value = '로그인에 실패했습니다. 다시 시도해주세요.'
  }
}
</script>

<style scoped>
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    line-height: 1.6;
    background: #fafafa;
    min-height: 100vh;
    overflow-x: hidden;
}

.main-container {
    display: flex;
    min-height: 100vh;
    width: 100%;
}

/* 왼쪽 브랜딩 영역 */
.brand-section {
    flex: 0 0 50%;  /* 정확히 50% 고정 */
    min-width: 50%;
    max-width: 50%;
    background: linear-gradient(135deg, #f97316 0%, #ea580c 50%, #dc2626 100%);
    position: relative;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 60px;
    color: white;
}

.brand-section::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
        radial-gradient(circle at 20% 50%, rgba(255,255,255,0.1) 0%, transparent 50%),
        radial-gradient(circle at 80% 20%, rgba(255,255,255,0.08) 0%, transparent 50%),
        radial-gradient(circle at 40% 80%, rgba(255,255,255,0.06) 0%, transparent 50%);
    pointer-events: none;
}

.brand-content {
    position: relative;
    z-index: 1;
    max-width: 480px;
}

.brand-logo {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 48px;
}

.brand-logo::before {
    content: "✦";
    font-size: 32px;
    color: white;
}

.brand-logo h1 {
    font-size: 32px;
    font-weight: 700;
    color: white;
}

.brand-title {
    font-size: 48px;
    font-weight: 700;
    line-height: 1.2;
    margin-bottom: 24px;
}

.brand-subtitle {
    font-size: 20px;
    line-height: 1.5;
    color: rgba(255, 255, 255, 0.9);
    margin-bottom: 48px;
}

.brand-features {
    display: flex;
    flex-direction: column;
    gap: 20px;
    margin-bottom: 48px;
}

.feature-item {
    display: flex;
    align-items: center;
    gap: 16px;
}

.feature-icon {
    width: 24px;
    height: 24px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

.feature-text {
    font-size: 16px;
    color: rgba(255, 255, 255, 0.95);
}

.brand-stats {
    display: flex;
    gap: 32px;
}

.stat-item {
    text-align: left;
}

.stat-number {
    font-size: 28px;
    font-weight: 700;
    margin-bottom: 4px;
}

.stat-label {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
}

/* 오른쪽 로그인 폼 영역 */
.login-section {
    flex: 0 0 50%;  /* 정확히 50% 고정 */
    min-width: 50%;
    max-width: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px;
    background: #fafafa;
}

.login-container {
    width: 100%;
    max-width: 400px;
}

.login-header {
    text-align: center;
    margin-bottom: 40px;
}

.login-title {
    font-size: 32px;
    font-weight: 700;
    color: #1f2937;
    margin-bottom: 8px;
}

.login-subtitle {
    font-size: 16px;
    color: #6b7280;
}

.alert {
    padding: 16px;
    border-radius: 12px;
    margin-bottom: 24px;
    font-size: 14px;
    font-weight: 500;
    display: none;
}

.alert-error {
    background: #fef2f2;
    color: #dc2626;
    border: 1px solid #fecaca;
}

.alert-success {
    background: #f0fdf4;
    color: #16a34a;
    border: 1px solid #bbf7d0;
}

.form-group {
    margin-bottom: 24px;
}

.form-label {
    display: block;
    font-size: 14px;
    font-weight: 600;
    color: #374151;
    margin-bottom: 8px;
}

.form-control {
    width: 100%;
    padding: 16px;
    border: 2px solid #e5e7eb;
    border-radius: 12px;
    font-size: 16px;
    background: white;
    color: #1f2937;
    transition: all 0.2s ease;
}

.form-control:focus {
    outline: none;
    border-color: #f97316;
    box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
}

.form-control::placeholder {
    color: #9ca3af;
}

.password-group {
    position: relative;
}

.password-toggle {
    position: absolute;
    right: 16px;
    top: 50%;
    transform: translateY(-50%);
    background: none;
    border: none;
    color: #6b7280;
    cursor: pointer;
    padding: 4px;
    font-size: 14px;
    font-weight: 500;
    transition: color 0.2s ease;
}

.password-toggle:hover {
    color: #374151;
}

.form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin: 24px 0;
    font-size: 14px;
}

.remember-me {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #6b7280;
    cursor: pointer;
}

.remember-me input[type="checkbox"] {
    width: 18px;
    height: 18px;
    accent-color: #f97316;
}

.forgot-link {
    color: #f97316;
    text-decoration: none;
    font-weight: 600;
    transition: color 0.2s ease;
}

.forgot-link:hover {
    color: #ea580c;
}

.login-btn {
    width: 100%;
    padding: 16px;
    background: #1f2937;
    color: white;
    border: none;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
    position: relative;
    overflow: hidden;
}

.login-btn:hover {
    background: #111827;
    transform: translateY(-2px);
    box-shadow: 0 10px 25px rgba(31, 41, 55, 0.2);
}

.login-btn:active {
    transform: translateY(0);
}

.login-btn:disabled {
    background: #9ca3af;
    cursor: not-allowed;
    transform: none;
    box-shadow: none;
}

.divider {
    display: flex;
    align-items: center;
    margin: 32px 0;
    color: #6b7280;
    font-size: 14px;
}

.divider::before,
.divider::after {
    content: '';
    flex: 1;
    height: 1px;
    background: #e5e7eb;
}

.divider span {
    padding: 0 16px;
    background: #fafafa;
}

.social-login {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.social-btn {
    width: 100%;
    padding: 16px;
    border: 2px solid #e5e7eb;
    border-radius: 12px;
    background: white;
    color: #374151;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    text-decoration: none;
}

.social-btn:hover {
    border-color: #d1d5db;
    background: #f9fafb;
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

.signup-section {
    text-align: center;
    margin-top: 32px;
    padding-top: 24px;
    border-top: 1px solid #e5e7eb;
    color: #6b7280;
    font-size: 14px;
}

.signup-link {
    color: #f97316;
    text-decoration: none;
    font-weight: 600;
    transition: color 0.2s ease;
}

.signup-link:hover {
    color: #ea580c;
}

.alert-modal {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: white;
  border-radius: 12px;
  padding: 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  z-index: 20;
  width: 80%;
  max-width: 360px;
  overflow: hidden;
}

.alert-content {
  padding: 24px;
  text-align: center;
  font-size: 16px;
  line-height: 1.5;
  color: #333;
}

.alert-footer {
  border-top: 1px solid #eee;
}

.overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 5;
  border-radius: 8px;
}

.confirm-btn {
  width: 100%;
  background-color: transparent;
  color: #557591;
  border: none;
  padding: 16px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
}

.confirm-btn:hover {
  background-color: #f7f7f7;
}

.loading {
    display: inline-block;
    width: 20px;
    height: 20px;
    border: 2px solid #ffffff40;
    border-radius: 50%;
    border-top-color: #ffffff;
    animation: spin 1s ease-in-out infinite;
}

@keyframes spin {
    to { transform: rotate(360deg); }
}

/* 반응형 디자인 */
@media (max-width: 1024px) {
    .brand-section {
        display: none;
    }
    
    .login-section {
        flex: none;
        width: 100%;
    }
    
    .main-container {
        background: linear-gradient(135deg, #f97316 0%, #ea580c 50%, #dc2626 100%);
    }
    
    .login-section {
        background: #fafafa;
        margin: 20px;
        border-radius: 20px;
        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
    }
}

/* @media (max-width: 640px) {
    .login-section {
        margin: 0;
        border-radius: 0;
        box-shadow: none;
        min-height: 100vh;
    }
    
    .login-container {
        max-width: none;
    }
    
    .brand-stats {
        flex-direction: column;
        gap: 16px;
    }
} */

/* 다크모드 지원 */
@media (prefers-color-scheme: dark) {
    body {
        background: #0f172a;
    }
    
    .login-section {
        background: #1e293b;
    }
    
    .login-title {
        color: #f8fafc;
    }
    
    .login-subtitle, .form-label {
        color: #cbd5e1;
    }
    
    .form-control {
        background: #334155;
        border-color: #475569;
        color: #f8fafc;
    }
    
    .form-control:focus {
        border-color: #f97316;
    }
    
    .social-btn {
        background: #334155;
        border-color: #475569;
        color: #f8fafc;
    }
    
    .social-btn:hover {
        background: #475569;
    }
    
    .divider span {
        background: #1e293b;
    }
    
    .divider::before,
    .divider::after {
        background: #475569;
    }
    
    .signup-section {
        border-color: #475569;
    }
}
</style>

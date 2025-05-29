<template>
    <div class="signup-page">
        <div class="signup-container">
            <h1 class="signup-title">회원가입</h1>
            
            <div class="required-notice">
                <span class="required-text">* 필수입력사항</span>
            </div>

            <div v-if="showSuccessMessage" class="success-message">
                {{ successMessage }}
            </div>

            <!-- 회원가입 폼 -->
            <form @submit.prevent="handleSignup" class="signup-form">
                <!-- 아이디 -->
                <div class="form-group">
                    <label class="form-label">
                        아이디<span class="required">*</span>
                    </label>
                    <div class="input-wrapper">
                        <input 
                            v-model="formData.loginId" 
                            type="text"
                            class="form-input"
                            placeholder="아이디를 입력해주세요"
                            required 
                        />
                        <button type="button" class="check-btn" @click="checkLoginId" :disabled="isCheckingId">
                            <span v-if="isCheckingId" class="spinner"></span>
                            <span v-else>중복확인</span>
                        </button>
                    </div>
                    <div class="message-area">
                        <span v-if="showLoginIdCheck === 'duplicate'" class="error-message">
                            이미 사용 중인 아이디입니다.
                        </span>
                        <span v-if="showLoginIdCheck === 'available'" class="success-message-text">
                            사용 가능한 아이디입니다.
                        </span>
                        <span v-if="showLoginIdCheck === 'error'" class="error-message">
                            중복 확인 중 오류가 발생했습니다.
                        </span>
                    </div>
                </div>

                <!-- 비밀번호 -->
                <div class="form-group">
                    <label class="form-label">
                        비밀번호<span class="required">*</span>
                    </label>
                    <input 
                        v-model="formData.password" 
                        type="password"
                        class="form-input"
                        placeholder="비밀번호 (문자, 숫자, 특수문자 포함 8~15자)를 입력해주세요"
                        @input="checkPasswordMatch"
                        required 
                    />
                </div>

                <!-- 비밀번호 확인 -->
                <div class="form-group">
                    <label class="form-label">
                        비밀번호확인<span class="required">*</span>
                    </label>
                    <input 
                        v-model="formData.passwordConfirm" 
                        type="password"
                        class="form-input"
                        placeholder="비밀번호를 다시 입력해주세요"
                        @input="checkPasswordMatch"
                        required 
                    />
                    <div class="message-area">
                        <span v-if="showPasswordMismatch" class="error-message">
                            비밀번호가 일치하지 않습니다.
                        </span>
                    </div>
                </div>

                <!-- 이름 -->
                <div class="form-group">
                    <label class="form-label">
                        이름<span class="required">*</span>
                    </label>
                    <input 
                        v-model="formData.name" 
                        type="text"
                        class="form-input"
                        placeholder="이름을 입력해주세요"
                        required 
                    />
                </div>

                <!-- 이메일 -->
                <div class="form-group">
                    <label class="form-label">
                        이메일<span class="required">*</span>
                    </label>
                    <input 
                        v-model="formData.email" 
                        type="email"
                        class="form-input"
                        placeholder="이메일을 입력해주세요"
                        required 
                    />
                </div>

                <!-- 전화번호 -->
                <div class="form-group">
                    <label class="form-label">
                        전화번호<span class="required">*</span>
                    </label>
                    <input 
                        v-model="formData.phone" 
                        type="tel"
                        class="form-input"
                        placeholder="전화번호를 입력해주세요"
                        required 
                    />
                </div>

                <!-- 생년월일 -->
                <div class="form-group">
                    <label class="form-label">생년월일</label>
                    <div class="birthdate-wrapper">
                        <input 
                            v-model="formData.birthYear" 
                            type="text"
                            class="birth-input"
                            placeholder="YYYY"
                            maxlength="4"
                            @input="validateBirthdate"
                        />
                        <span class="separator">/</span>
                        <input 
                            v-model="formData.birthMonth" 
                            type="text"
                            class="birth-input"
                            placeholder="MM"
                            maxlength="2"
                            @input="validateBirthdate"
                        />
                        <span class="separator">/</span>
                        <input 
                            v-model="formData.birthDay" 
                            type="text"
                            class="birth-input"
                            placeholder="DD"
                            maxlength="2"
                            @input="validateBirthdate"
                        />
                    </div>
                    <div class="message-area">
                        <span v-if="birthdateWarning" class="error-message">
                            올바른 생년월일을 입력해주세요.
                        </span>
                    </div>
                </div>

                <!-- 회원가입 버튼 -->
                <button type="submit" class="signup-btn" :disabled="isLoading">
                    <span v-if="isLoading" class="loading-content">
                        <span class="spinner"></span>
                        <span>회원가입 중...</span>
                    </span>
                    <span v-else>회원가입</span>
                </button>

                <!-- 로그인 링크 -->
                <div class="login-link">
                    이미 계정이 있으신가요? 
                    <router-link to="/login" class="link-text">로그인</router-link>
                </div>
            </form>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const formData = reactive({
    loginId: '',
    password: '',
    passwordConfirm: '',
    name: '',
    email: '',
    phone: '',
    birthYear: '',
    birthMonth: '',
    birthDay: ''
})

const showLoginIdCheck = ref(null)
const showPasswordCheck = ref(false)
const showSuccessMessage = ref(false)
const successMessage = ref('')
const birthdateWarning = ref(false)

// 아이디 중복확인 API 호출
async function checkLoginId() {
    if (!formData.loginId) {
        showLoginIdCheck.value = null
        return
    }

    try {
        const response = await axios.get(`/accounts/${formData.loginId}/exists`)
        const isDuplicate = response.data

        if (isDuplicate) {
            showLoginIdCheck.value = 'duplicate'
        } else {
            showLoginIdCheck.value = 'available'
        }
    } catch (error) {
        console.error('아이디 중복 확인 오류:', error)
        showLoginIdCheck.value = 'error'
    }
}

// 비밀번호 일치 확인
function checkPasswordMatch() {
    if (formData.password && formData.passwordConfirm) {
        showPasswordMismatch.value = formData.password !== formData.passwordConfirm 
    } else {
        showPasswordMismatch.value = false
    }
}

// 생년월일 유효성 검사
function validateBirthdate() {
    const year = parseInt(formData.birthYear)
    const month = parseInt(formData.birthMonth)
    const day = parseInt(formData.birthDay)

    if (formData.birthYear && formData.birthMonth && formData.birthDay) {
        const currentYear = new Date().getFullYear()
        
        birthdateWarning.value = !(
            year >= 1900 && year <= currentYear &&
            month >= 1 && month <= 12 &&
            day >= 1 && day <= 31
        )
    } else {
        birthdateWarning.value = false
    }
}

// 회원가입 처리
async function handleSignup() {
    // 필수 입력값 검증
    if (!formData.loginId || !formData.password || !formData.passwordConfirm 
    || !formData.name || !formData.email || !formData.phone 
    || !formData.birthYear || !formData.birthMonth || !formData.birthDay) {
        alert('모든 필수 항목을 입력해주세요.')
        return
    }

    // 비밀번호 확인
    if (!formData.password || !formData.passwordConfirm) {
        showPasswordCheck.value = true
    }

    // 아이디 중복확인 체크
    if (showLoginIdCheck.value === 'duplicate') {
        alert('이미 사용 중인 아이디입니다.')
        return
    }

    try {
        const response = await axios.post('/accounts/signup', formData)
        showSuccessMessage.value = true
        successMessage.value = '회원가입이 완료되었습니다.'
    } catch (error) {
        console.error('회원가입 오류:', error)
        
        if (error.response?.status === 409) {
            alert('이미 사용 중인 아이디입니다.')
        } else {
            alert('회원가입에 실패했습니다. 다시 시도해주세요.')
        }
    }
}
</script>
<style scoped>
.signup-page {
    min-height: 100vh;
    background-color: white;
    display: flex;
    align-items: flex-start;
    justify-content: center;
    padding: 40px 20px;
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    width: 100%;
    box-sizing: border-box;
}

.signup-container {
    width: 100%;
    max-width: 420px;
    background: white;
    border-radius: 0;
    box-shadow: none;
    padding: 40px 40px;
    border: none;
}

.signup-title {
    font-size: 28px;
    font-weight: 700;
    color: #333;
    text-align: center;
    margin: 0 0 30px 0;
}

.required-notice {
    text-align: right;
    margin-bottom: 30px;
    padding-bottom: 20px;
    border-bottom: 2px solid #333;
}

.required-text {
    color: #888;
    font-size: 12px;
}

.signup-form {
    display: flex;
    flex-direction: column;
}

.form-group {
    margin-bottom: 25px;
}

.form-label {
    display: block;
    font-size: 14px;
    font-weight: 600;
    color: #333;
    margin-bottom: 8px;
}

.required {
    color: #e74c3c;
    margin-left: 2px;
}

.form-input {
    width: 100%;
    padding: 15px 16px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 16px;
    background: #f8f9fa;
    transition: all 0.2s;
    box-sizing: border-box;
}

.form-input:focus {
    outline: none;
    border-color: #FFBB3F;
    background: white;
    box-shadow: 0 0 0 2px rgba(108, 92, 231, 0.1);
}

.form-input::placeholder {
    color: #999;
}

/* 아이디 입력 */
.input-wrapper {
    display: flex;
    gap: 10px;
}

.input-wrapper .form-input {
    flex: 1;
}

/* 생년월일 */
.birthdate-wrapper {
    display: flex;
    align-items: center;
    border: 1px solid #ddd;
    border-radius: 4px;
    padding: 0;
    background-color: white;
    overflow: hidden;
}

.birth-input {
    border: none;       
    outline: none;                     
    padding: 15px 16px;               
    font-size: 14px;
    background: transparent;
    flex: 1;                         
    text-align: center;              
}

.birth-input:focus {
    outline: none;
    border-color: #6c5ce7;
    background: white;
}

.separator {
    color: #666;
    font-size: 16px;
    font-weight: 500;
}

/* 메시지 영역 */
.message-area {
    height: 18px;
    margin-top: 5px;
}

.error-message {
    color: #e74c3c;
    font-size: 12px;
}

.success-message-text {
    color: #27ae60;
    font-size: 12px;
}

/* 중복확인 버튼 */
.check-btn {
    padding: 15px 20px;
    background: white;
    color: #333;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 14px;
    cursor: pointer;
    white-space: nowrap;
    transition: background 0.2s;
    min-width: 100px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.check-btn:hover:not(:disabled) {
    background: #f1f1f1;
}

.check-btn:disabled {
    background: #f8f9fa;
    color: #999;
    cursor: not-allowed;
}

/* 회원가입 버튼 */
.signup-btn {
    width: 100%;
    padding: 18px;
    background: #6c5ce7;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    margin-top: 30px;
    transition: background 0.2s;
    position: relative;
}

.signup-btn:hover:not(:disabled) {
    background: #5b4cdb;
}

.signup-btn:disabled {
    background: #a8a8a8;
    cursor: not-allowed;
}

.loading-content {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
}

/* 스피너 애니메이션 */
.spinner {
    width: 16px;
    height: 16px;
    border: 2px solid transparent;
    border-top: 2px solid currentColor;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    display: inline-block;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

/* 로그인 링크 */
.login-link {
    text-align: center;
    margin-top: 20px;
    font-size: 14px;
    color: #666;
}

.link-text {
    color: #6c5ce7;
    text-decoration: none;
    font-weight: 600;
}

.link-text:hover {
    text-decoration: underline;
}

/* 성공 메시지 */
.success-message {
    background: #d4edda;
    color: #155724;
    padding: 12px 16px;
    border-radius: 4px;
    margin-bottom: 20px;
    text-align: center;
    font-size: 14px;
}

/* 반응형 */
@media (max-width: 768px) {
    .signup-container {
        padding: 30px 20px;
        margin: 20px;
    }
    
    .input-wrapper {
        flex-direction: column;
        gap: 10px;
    }
    
    .birthdate-wrapper {
        flex-direction: column;
        gap: 10px;
    }
    
    .birth-input {
        text-align: left;
    }
}
</style>
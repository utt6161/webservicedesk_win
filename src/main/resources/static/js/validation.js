Vue.use(window.vuelidate.default);

const { required, minLength, maxLength, helpers, alphaNum, alpha, email, sameAs } = window.validators;

//const strongPass = helpers.regex('strongPass', /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\S+$).{6,}$/);

// const ru_alpha = helpers.regex('ru_alpha', '^[А-Яа-я]+$');

new Vue({
    name: 'form-validate',
    el: '#app',
    delimiters: ["<%","%>"],

    data:{

        last_name: '',
        first_name: '',
        middle_name: '',
        username: '',
        password: '',
        password_match: '',
        phone_number: '',
        email: ''

    },

    validations: {

        first_name: {
            required,
            minLength: minLength(1),
            maxLength: maxLength(45),
            ru_alpha: val => /^[A-Za-zА-Яа-я]+$/.test(val),
        },
        last_name: {
            required,
            minLength: minLength(1),
            maxLength: maxLength(45),
            ru_alpha: val => /^[A-Za-zА-Яа-я]+$/.test(val),
        },
        middle_name: {
            minLength: minLength(0),
            maxLength: maxLength(45),
            ru_alpha: val => /^[A-Za-zА-Яа-я]*$/.test(val),
        },
        username: {
            required,
            minLength: minLength(5),
            maxLength: maxLength(20),
            alphaNum
        },
        password: {
            required,
            minLength: minLength(4),
            maxLength: maxLength(15),
            strongPass: val => /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\S+$).{4,15}$/.test(val),
        },
        password_match:{
            sameAs: sameAs('password'),
            minLength: minLength(4),
            maxLength: maxLength(15),
            strongPass: val => /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\S+$).{4,15}$/.test(val),
        },
        phone_number: {
            required,
            phone: val => /^((\+7|7|8)+([0-9]){10})$/.test(val),
        },
        email: {
            required,
            email
        }

    },


    methods: {
        submit(event){
            if(this.$v.$invalid){
                event.preventDefault();
                this.$v.$touch();
            }
        },
        status(validation) {
            return {
            error: validation.$error,
            dirty: validation.$dirty
          }
        }
  }

});
Vue.use(window.vuelidate.default);

const { required, minLength, maxLength, helpers, sameAs } = window.validators;

//const strongPass = helpers.regex('strongPass', /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\S+$).{6,}$/);

// const ru_alpha = helpers.regex('ru_alpha', '^[А-Яа-я]+$');

new Vue({
    name: 'form-validate',
    el: '#app',
    delimiters: ["<%","%>"],

    data:{

        password: '',
        password_match: '',

    },

    validations: {


        password: {
            required,
            minLength: minLength(4),
            maxLength: maxLength(15),
            strongPass: val => /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\S+$).{4,15}$/.test(val),
        },
        password_match: {
            sameAs: sameAs('password'),
            minLength: minLength(4),
            maxLength: maxLength(15),
            strongPass: val => /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\S+$).{4,15}$/.test(val),
        },
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
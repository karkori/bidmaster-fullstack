import { z } from 'zod';

// Esquema de validación para el formulario de inicio de sesión
export const loginSchema = z.object({
  username: z
    .string()
    .min(3, {
      message: 'El nombre de usuario debe tener al menos 3 caracteres',
    })
    .max(50, {
      message: 'El nombre de usuario no puede tener más de 50 caracteres',
    }),
  password: z
    .string()
    .min(8, { message: 'La contraseña debe tener al menos 8 caracteres' }),
});

// Esquema de validación para el formulario de registro
export const registerSchema = z
  .object({
    username: z
      .string()
      .min(3, {
        message: 'El nombre de usuario debe tener al menos 3 caracteres',
      })
      .max(50, {
        message: 'El nombre de usuario no puede tener más de 50 caracteres',
      }),
    email: z
      .string()
      .email({ message: 'Por favor, ingresa un correo electrónico válido' }),
    password: z
      .string()
      .min(8, { message: 'La contraseña debe tener al menos 8 caracteres' })
      .regex(/[A-Z]/, {
        message: 'La contraseña debe contener al menos una letra mayúscula',
      })
      .regex(/[0-9]/, {
        message: 'La contraseña debe contener al menos un número',
      }),
    confirmPassword: z.string().min(1, { message: 'Confirma tu contraseña' }),
    firstName: z.string().optional(),
    lastName: z.string().optional(),
    phone: z.string().optional(),
    termsAccepted: z.boolean().refine((val) => val === true, {
      message: 'Debes aceptar los términos y condiciones',
    }),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Las contraseñas no coinciden',
    path: ['confirmPassword'],
  });

// Tipos derivados de los esquemas
export type LoginFormType = z.infer<typeof loginSchema>;
export type RegisterFormType = z.infer<typeof registerSchema>;

# Guía de Mejores Prácticas para el Frontend (Angular 19)

Este documento establece las directrices y estándares para el desarrollo del frontend de BidMaster usando Angular 19.

## Arquitectura y Estructura de Componentes

### Organización de Componentes
Para una mejor organización y mantenimiento, los componentes deben clasificarse en:

- **Componentes específicos de página**: ubicados en `app/components/`
  - Solo se utilizan dentro de una página o sección específica
  - No deben reutilizarse en otras partes de la aplicación
  - Pueden organizarse por dominio: `components/dashboard/auction-form/`

- **Componentes compartidos**: ubicados en `app/shared/components/`
  - Reutilizables en cualquier parte de la aplicación
  - Deben tener una API clara y documentada
  - Ejemplos: botones, campos de formulario, modales, tablas

### Separación de Plantillas HTML y Lógica TypeScript
Se debe separar las plantillas HTML en archivos independientes de los componentes TypeScript para:
- Mejorar la legibilidad del código
- Facilitar el mantenimiento
- Permitir una clara separación de responsabilidades
- Mejorar la experiencia de desarrollo con herramientas específicas para cada lenguaje

```typescript
// RECOMENDADO
@Component({
  selector: 'app-my-component',
  templateUrl: './my-component.component.html',
  styleUrls: ['./my-component.component.css']
})
export default class MyComponent { }
```

En lugar de:

```typescript
// EVITAR para componentes complejos
@Component({
  selector: 'app-my-component',
  template: `<div>Plantilla inline que dificulta la lectura cuando crece</div>`,
  styles: [`.my-style { color: red; }`]
})
export default class MyComponent { }
```

### Componentes Standalone
En Angular 19, los componentes son standalone por defecto. Esto significa que:
- No es necesario especificar `standalone: true` en el decorador `@Component`
- Los componentes pueden importarse directamente en otros componentes sin necesidad de un módulo

```typescript
@Component({
  selector: 'app-my-component',
  // No es necesario especificar standalone: true
  imports: [CommonModule, ReactiveFormsModule],
  template: `...`
})
export default class MyComponent { }
```

### Exportación por Defecto para Componentes de Página
Los componentes utilizados como páginas en las rutas deben exportarse como `default` para simplificar el enrutamiento:

```typescript
// CORRECTO
export default class LoginComponent { }

// EN EL ROUTER
{
  path: 'login',
  loadComponent: () => import('./pages/auth/login/login.component')
}
```

En lugar de:

```typescript
// INCORRECTO para componentes de página
export class LoginComponent { }

// EN EL ROUTER (requiere then)
{
  path: 'login',
  loadComponent: () => import('./pages/auth/login/login.component').then(m => m.LoginComponent)
}
```

## Inyección de Dependencias

### Uso de la función inject()
Preferir el uso de la función `inject()` en lugar de la inyección por constructor para:
- Tener un código más limpio y conciso
- Reducir la verbosidad de las clases
- Mejorar la legibilidad separando las inyecciones de la lógica de negocio

```typescript
// RECOMENDADO: Uso de inject()
export class MyComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  
  // Lógica del componente sin constructor
}
```

En lugar de:

```typescript
// EVITAR: Inyección por constructor
export class MyComponent {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}
  
  // Lógica del componente
}
```

## Gestión de Estado y Reactividad

### Uso de Signals
Usar signals en lugar de BehaviorSubject/Observable siempre que sea posible para:
- Mejorar el rendimiento
- Simplificar el código
- Aprovechar la detección de cambios optimizada

```typescript
// RECOMENDADO: Usar signals
export class AuthService {
  currentUser = signal<User | null>(null);
  isAuthenticated = computed(() => !!this.currentUser());
  
  login(user: User) {
    this.currentUser.set(user);
  }
  
  logout() {
    this.currentUser.set(null);
  }
}

// EN COMPONENTE
@Component({
  template: `
    @if (authService.isAuthenticated()) {
      <p>Bienvenido, {{ authService.currentUser()?.name }}</p>
    }
  `
})
export default class MyComponent {
  constructor(public authService: AuthService) {}
}
```

## Herramientas y Dependencias

### Gestión de Paquetes
Usar Bun en lugar de npm o yarn para instalación de dependencias:

```bash
# RECOMENDADO
bun add zod @angular/material

# EVITAR
npm install zod @angular/material
```

### Formularios y Validación

### Componentes de Formulario Reutilizables
Crear componentes reutilizables para los elementos de formulario comunes:

- **InputFieldComponent**: Para campos de texto, número, email, etc.
- **TextareaFieldComponent**: Para texto multilínea
- **SelectFieldComponent**: Para menús desplegables
- **CheckboxFieldComponent**: Para casillas de verificación
- **FormGroupComponent**: Para agrupar campos relacionados

#### Implementación de ControlValueAccessor
Los componentes de formulario deben implementar ControlValueAccessor para integrarse correctamente con Angular Reactive Forms:

```typescript
@Component({
  selector: 'app-input-field',
  templateUrl: './input-field.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => InputFieldComponent),
      multi: true
    }
  ]
})
export class InputFieldComponent implements ControlValueAccessor {
  @Input() label: string = '';
  @Input() required: boolean = false;
  @Input() errorMessage: string = '';
  
  value: any = '';
  disabled: boolean = false;
  onChange = (value: any) => {};
  onTouched = () => {};
  
  writeValue(value: any): void { this.value = value; }
  registerOnChange(fn: any): void { this.onChange = fn; }
  registerOnTouched(fn: any): void { this.onTouched = fn; }
  setDisabledState(isDisabled: boolean): void { this.disabled = isDisabled; }
}
```

### Formularios reactivos con arrays dinámicos
Para interfaces que requieren generar elementos dinámicamente (como sidebar, menús, etc.):

```typescript
// Definir interfaz para los items
interface SidebarItem {
  label: string;
  route: string;
  icon: string;
  requiresAdmin?: boolean;
}

// Crear array de items
sidebarItems: SidebarItem[] = [
  { label: 'Mi Perfil', route: '/dashboard/profile', icon: 'user' },
  { label: 'Mis Pujas', route: '/dashboard/bids', icon: 'hammer' }
];

// En el template, usar @for para generar el contenido
@for (item of sidebarItems; track item.route) {
  <a [routerLink]="item.route" class="sidebar-item">
    <svg-icon [name]="item.icon"></svg-icon>
    {{ item.label }}
  </a>
}
```

### Validación de Formularios
- Usar Zod para la validación de esquemas y formularios
- Integrar Zod con Reactive Forms para tener un sistema de validación robusto

```typescript
// Definir esquema con Zod
const loginSchema = z.object({
  username: z.string().min(3),
  password: z.string().min(8)
});

// Validar en componente
validateFormWithZod() {
  try {
    loginSchema.parse(this.loginForm.value);
  } catch (error) {
    if (error instanceof z.ZodError) {
      // Manejar errores
    }
  }
}
```

## Rendimiento y Optimización

- Preferir la exportación por defecto para los componentes de página para optimizar la carga diferida
- Usar signals en lugar de observables para mejorar el rendimiento
- Implementar estrategias de memorización con `computed()` para cálculos costosos
- Usar `@defer` en plantillas para componentes pesados que puedan cargarse de forma diferida
- Utilizar `position: sticky` en lugar de `position: absolute` para elementos de navegación o sidebar que deban mantener su posición durante el scroll
- Cuando se desarrollen layouts complejos, preferir flexbox para una mejor gestión del espacio y responsive design

## Estilos y UI

### Diseño de Formularios
- Mantener una estética consistente en todos los formularios
- Usar componentes reutilizables para mantener la coherencia visual
- Proporcionar retroalimentación clara sobre errores de validación
- Agrupar campos relacionados visualmente (usando `FormGroupComponent`)
- Incluir instrucciones o textos de ayuda cuando sea necesario

### Responsive Design
- Diseñar pensando primero en dispositivos móviles (mobile-first)
- Utilizar las utilidades de Tailwind para adaptar la interfaz a diferentes tamaños de pantalla:
  ```html
  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
  ```
- Probar la interfaz en múltiples dispositivos y tamaños de pantalla

---

Estas directrices se actualizarán a medida que el proyecto evolucione y se identifiquen nuevas mejores prácticas.

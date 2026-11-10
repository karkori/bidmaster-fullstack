import { Component, computed, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';

interface FilterOption {
  value: string;
  label: string;
}

@Component({
  selector: 'app-filter-bar',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './filter-bar.component.html'
})
export default class FilterBarComponent {
  filterOptions = input<FilterOption[]>([]);
  currentFilter = input<string>('all');
  searchPlaceholder = input<string>('Buscar...');
  color = input<'blue' | 'pink' | 'green' | 'indigo'>('blue');
  
  filterChange = output<string>();
  searchChange = output<string>();
  
  colorClasses = computed(() => {
    switch (this.color()) {
      case 'pink':
        return 'bg-pink-50 border-pink-100 text-pink-800 hover:bg-pink-100';
      case 'green':
        return 'bg-green-50 border-green-100 text-green-800 hover:bg-green-100';
      case 'indigo':
        return 'bg-indigo-50 border-indigo-100 text-indigo-800 hover:bg-indigo-100';
      default: // blue
        return 'bg-blue-50 border-blue-100 text-blue-800 hover:bg-blue-100';
    }
  });
  
  activeButtonClasses = computed(() => {
    switch (this.color()) {
      case 'pink':
        return 'bg-pink-100';
      case 'green':
        return 'bg-green-100';
      case 'indigo':
        return 'bg-indigo-100';
      default: // blue
        return 'bg-blue-100';
    }
  });
  
  onFilterClick(value: string): void {
    this.filterChange.emit(value);
  }
  
  onSearch(event: any): void {
    this.searchChange.emit(event.target.value);
  }
}

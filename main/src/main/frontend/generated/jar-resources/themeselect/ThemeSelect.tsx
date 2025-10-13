import "./theme-select";

type Props = {
  className?: string;
  label?: string;
};

export function ThemeSelect({ className, label }: Props) {
  // @ts-ignore
  return <theme-select class={className} label={label} />;
}
